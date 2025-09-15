$(document).ready(function() {
	// Global variables
	let selectedDocument = null;
	let uploadedMetadataFile = null;
	let uploadedZipFile = null;
	let updateUploadedMetadataFile = null;
	let updateUploadedZipFile = null;
	let mainTableInstance = null;
	let draftTableInstance = null;
	let currentUpdateRow = null;
	let columnFilters = {};

	// Folder and Sub-folder dependency mapping
	const folderSubFolderMap = {

		/*'Drawing': ['MJ RUBs', 'ROBs', 'MJBs', 'MNB MNRUB', 'Earthwork', 'Structural', 'Architectural', 'MEP'],
		'Correspondence': ['Internal', 'External', 'Client', 'Contractor', 'Consultant'],
		'MOMs': ['Site Meeting', 'Progress Meeting', 'Safety Meeting', 'Technical Meeting', 'Coordination Meeting'],
		'Report': ['Progress Report', 'Technical Report', 'Safety Report', 'Quality Report', 'Inspection Report'],
		'Work Programme': ['Monthly', 'Weekly', 'Daily', 'Milestone', 'Critical Path'],
		'Manuals': ['Operation Manual', 'Maintenance Manual', 'Safety Manual', 'Quality Manual', 'Technical Manual'],
		'RFI': ['Technical RFI', 'Commercial RFI', 'Design RFI', 'Construction RFI'],
		'Contracts': ['Main Contract', 'Sub Contract', 'Amendment', 'Variation Order', 'Agreement'],
		'SHE': ['Safety Plan', 'Incident Report', 'Audit Report', 'Training Record', 'Risk Assessment'],
		'Quality': ['Quality Plan', 'Test Certificate', 'Inspection Report', 'Non Conformance', 'Quality Audit'],
		'Billing': ['Invoice', 'Payment Certificate', 'Variation Bill', 'Final Bill', 'Interim Bill'],
		'Legal': ['Legal Notice', 'Agreement', 'Compliance', 'Dispute', 'Contract Review']*/
	};

	// Initialize DataTables
	function initializeDataTables() {
		if (!mainTableInstance && $('#mainTable').length) {
			mainTableInstance = $('#mainTable').DataTable({
				"language": {
					"lengthMenu": "Show _MENU_ entries",
					"info": "Showing _START_ to _END_ of _TOTAL_ entries"
				},
				"pageLength": 10,
				"destroy": true,
				"drawCallback": function() {
					updateAllColumnFilters();
				}
			});
		}

		if (!draftTableInstance && $('#draftTable').length) {
			draftTableInstance = $('#draftTable').DataTable({
				"language": {
					"lengthMenu": "Show _MENU_ entries",
					"info": "Showing _START_ to _END_ of _TOTAL_ entries",
					"emptyTable": "No drafts available"
				},
				"pageLength": 10,
				"destroy": true
			});
		}
	}

	// Initialize tables on page load
	initializeDataTables();



	// Function to update sub-folder options based on selected folder
	function updateSubFolderOptions(folderSelectId, subFolderSelectId) {
		const folderValue = $(folderSelectId).val();
		const subFolderSelect = $(subFolderSelectId);

		// Clear existing options
		subFolderSelect.empty().append('<option value="">Select Sub-Folder</option>');

		// inititialize folder dropdown
		$.ajax({
			url: '/dms/api/subfolders/' + folderValue,  // Replace with your actual API endpoint
			method: 'GET',
			success: function(data) {
				// Clear previous options except the default
				$(subFolderSelectId).find('option:not(:first)').remove();

				// Append new options
				$.each(data, function(index, subfolder) {
					$(subFolderSelectId).append(
						$('<option>', {
							value: subfolder.id,
							text: subfolder.name
						})
					);
				});
			},
			error: function(xhr) {
				console.error('Failed to load departments:', xhr.responseText);
			}
		});

		if (folderValue && folderSubFolderMap[folderValue]) {
			folderSubFolderMap[folderValue].forEach(function(subFolder) {
				subFolderSelect.append(`<option value="${subFolder}">${subFolder}</option>`);
			});
		}
	}

	// Event handlers for folder/sub-folder dependency
	$('#folder').change(function() {
		updateSubFolderOptions('#folder', '#subFolder');
	});

	$('#updateFolder').change(function() {
		updateSubFolderOptions('#updateFolder', '#updateSubFolder');
	});

	$('#previewFolder').change(function() {
		updateSubFolderOptions('#previewFolder', '#previewSubFolder');
	});

	// Notification system
	function showNotification(message, type = 'info') {
		const notification = $(`
                    <div class="notification ${type}">
                        ${message}
                    </div>
                `);

		$('#notificationContainer').append(notification);
		notification.fadeIn();

		setTimeout(() => {
			notification.fadeOut(() => {
				notification.remove();
			});
		}, 3000);
	}

	// Field validation functions
	function validateField(field) {
		const $field = $(field);
		const value = $field.val().trim();
		const fieldType = $field.attr('type');
		const isRequired = $field.closest('.form-group, .upload-form-section, .preview-form-section').find('label').text().includes('*');

		$field.removeClass('error-field success-field');
		$field.siblings('.error-message').removeClass('show');

		if (isRequired && (!value || value === '')) {
			$field.addClass('error-field');
			$field.siblings('.error-message').addClass('show');
			return false;
		} else if (fieldType === 'email' && value && !isValidEmail(value)) {
			$field.addClass('error-field');
			$field.siblings('.error-message').text('Please enter a valid email address').addClass('show');
			return false;
		} else if (value) {
			$field.addClass('success-field');
			return true;
		}

		return true;
	}

	function validateForm(formSelector) {
		let isValid = true;
		const $form = $(formSelector);

		$form.find('input, select, textarea').each(function() {
			if (!validateField(this)) {
				isValid = false;
			}
		});

		return isValid;
	}

	// Real-time validation
	$(document).on('input change blur', 'input, select, textarea', function() {
		validateField(this);
	});

	// Upload button click - show modal
	$('#uploadBtn').click(function(e) {
		e.preventDefault();
		$('#uploadModal').css('display', 'flex');
	});

	$('#documentSaveBtn').click(function(e) {
		e.preventDefault();
		var fileName = $('#fileName').val();
		var fileNumber = $('#fileNumber').val();
		var revisionNo = $('#revisionNo').val();
		var revisionDate = $('#revisionDate').val();
		var folder = $('#folder option:selected').text();
		var subFolder = $('#subFolder option:selected').text();
		var department = $('#department option:selected').text();
		var currentStatus = $('#currentStatus option:selected').text();
		//singleFileInput = $('#currentStatus').val();
		var files = $('#singleFileInput')[0].files;

		// Validate file
		if (!files && files.length <= 0) {
			alert("Please select a file.");
			return;
		}

		// Create FormData object
		var formData = new FormData();
		formData.append("fileName", fileName);
		formData.append("fileNumber", fileNumber);
		formData.append("revisionNo", revisionNo);
		formData.append("revisionDate", revisionDate);
		formData.append("folder", folder);
		formData.append("subFolder", subFolder);
		formData.append("department", department);
		formData.append("currentStatus", currentStatus);
		for (let i = 0; i < files.length; i++) {
			formData.append("files", files[i]); // key must match backend param
		}

		// Send AJAX request
		$.ajax({
			url: '/dms/api/documents',  // Your Spring Boot endpoint
			type: 'POST',
			data: formData,
			processData: false,
			contentType: false,
			success: function(responseData, textStatus, jqXHR) {
				if (responseData.errorMessage !== null) {
					alert(responseData.errorMessage);
				}
				else {
					$('#uploadModal').fadeOut();
					$('#successMessage').text("Successfully uploaded " + files.length + " files(s)");
					$('#successMessage').fadeIn(200).delay(2000).fadeOut(200);

				}
			},
			error: function(xhr, status, error) {
				alert("Upload failed: " + xhr.responseText);
			}
		});
	});

	// Draft button click: show draft table, hide main table
	$('#draftBtn').click(function() {
		$('.table-container').hide();
		$('#draftTableContainer').show();
		$('#backToMainBtn').show();

		if (draftTableInstance) {
			draftTableInstance.draw();
		}
	});

	// Back to Documents button handler
	$('#backToMainBtn').click(function() {
		$('#draftTableContainer').hide();
		$('#mainTableContainer').show();
		$(this).hide();
	});

	// Upload modal tabs
	$('#uploadModal .upload-tab').click(function() {
		$('#uploadModal .upload-tab').removeClass('active');
		$(this).addClass('active');

		const tabType = $(this).data('tab');
		$('.single-upload-options, .bulk-upload-options').removeClass('active');

		if (tabType === 'single') {
			$('#singleUploadTab').addClass('active');
		} else {
			$('#bulkUploadTab').addClass('active');
		}
	});

	// Update modal tabs
	$('#updateDocumentsModal .upload-tab').click(function() {
		$('#updateDocumentsModal .upload-tab').removeClass('active');
		$(this).addClass('active');

		const tabType = $(this).data('tab');
		$('.single-update-options, .bulk-update-options').removeClass('active');

		if (tabType === 'single') {
			$('#singleUpdateTab').addClass('active');
		} else {
			$('#bulkUpdateTab').addClass('active');
		}
	});

	// Close upload modal
	$('#cancelUpload').click(function() {
		$('#uploadModal').hide();
		resetUploadForm();
	});

	// Close modal when clicking outside - Upload
	$('#uploadModal').click(function(e) {
		if (e.target === this) {
			$(this).hide();
			resetUploadForm();
		}
	});

	// Save upload
	$('#saveUpload').click(function() {
		const activeTab = $('#uploadModal .upload-tab.active').data('tab');
		if (activeTab === 'single') {
			handleSingleUpload();
		} else {
			handleBulkUpload();
		}
	});

	// Right-click context menu for file name cells
	$(document).on('contextmenu', '.file-name-cell', function(e) {
		e.preventDefault();
		const contextMenu = $('#contextMenu');

		contextMenu.css({
			top: e.pageY + 'px',
			left: e.pageX + 'px',
			display: 'block'
		});

		contextMenu.data('target-cell', $(this));
		return false;
	});

	// Hide context menu on click elsewhere
	$(document).click(function() {
		$('#contextMenu').hide();
	});

	// Handle context menu item clicks
	$('.context-menu-item').click(function() {
		const action = $(this).data('action');
		const targetCell = $('#contextMenu').data('target-cell');

		handleContextMenuAction(action, targetCell);
		$('#contextMenu').hide();
	});

	// Send Documents Modal handlers
	$('#cancelSend').click(function() {
		$('#sendDocumentsModal').hide();
		resetSendForm();
	});

	$('#sendDocumentsModal').click(function(e) {
		if (e.target === this) {
			$(this).hide();
			resetSendForm();
		}
	});

	$('#sendDocument').click(function() {
		if (validateForm('#sendDocumentsModal')) {
			showNotification('Document sent successfully!', 'success');
			$('#sendDocumentsModal').hide();
			resetSendForm();
		}
	});

	// Save Draft button in Send Documents modal
	$('#saveDraft').click(function() {
		const to = $('#sendTo').val().trim();
		const cc = $('#sendCc').val().trim();
		const subject = $('#sendSubject').val().trim();
		const reason = $('#sendReason').val().trim();
		const responseExpected = $('#responseExpected').val();
		const targetDate = $('#targetResponseDate').val();
		const attachments = [];

		$('#attachmentsList .attachment-item span').each(function() {
			attachments.push($(this).text());
		});

		const dateSaved = new Date();
		const formattedDate = ('0' + dateSaved.getDate()).slice(-2) + '.' + ('0' + (dateSaved.getMonth() + 1)).slice(-2) + '.' + dateSaved.getFullYear();

		if (draftTableInstance) {
			draftTableInstance.row.add([
				to,
				cc,
				subject,
				reason,
				responseExpected ? (responseExpected.charAt(0).toUpperCase() + responseExpected.slice(1)) : '',
				targetDate ? formatDateForDisplay(targetDate) : '',
				attachments.join('<br>'),
				formattedDate
			]).draw();
		}

		showNotification('Draft saved successfully!', 'success');
		$('#sendDocumentsModal').hide();
		resetSendForm();
	});

	// Update Documents Modal handlers
	$('#cancelUpdate').on('click', function() {
		$('#updateDocumentsModal').removeClass('show');
		currentUpdateRow = null;
	});

	$('#updateDocumentsModal').click(function(e) {
		if (e.target === this) {
			$(this).hide();
			resetUpdateForm();
		}
	});

	$('#updateDocument').on('click', function() {
		const activeTab = $('#updateDocumentsModal .upload-tab.active').data('tab');
		if (activeTab === 'single') {
			if (!currentUpdateRow) return;

			const newFileName = $('#updateFileName').val();
			const newFileNumber = $('#updateFileNumber').val();
			const newRevisionNo = $('#updateRevisionNo').val();
			const newRevisionDate = $('#updateRevisionDate').val();
			const newFolder = $('#updateFolder').val();
			const newSubFolder = $('#updateSubFolder').val();
			const newDepartment = $('#updateDepartment').val();
			const newStatus = $('#updateCurrentStatus').val();

			// Update table row with new column positions
			currentUpdateRow.find('td').eq(2).find('input').val(newFileName);
			currentUpdateRow.find('td').eq(1).find('input').val(newFileNumber);
			currentUpdateRow.find('td').eq(3).find('input').val(newRevisionNo);
			currentUpdateRow.find('td').eq(10).find('input').val(newRevisionDate);
			currentUpdateRow.find('td').eq(6).find('input').val(newFolder);
			currentUpdateRow.find('td').eq(7).find('input').val(newSubFolder);
			currentUpdateRow.find('td').eq(11).find('input').val(newDepartment);
			currentUpdateRow.find('td').eq(4).find('input').val(newStatus);

			$('#updateDocumentsModal').removeClass('show');
			currentUpdateRow = null;
		} else if (activeTab === 'bulk') {
			alert('Bulk update saved (implement your logic here)');
			$('#updateDocumentsModal').removeClass('show');
			currentUpdateRow = null;
		}
	});

	// Bulk upload link handlers
	$('#downloadTemplate').click(function(e) {
		e.preventDefault();
		showNotification('Excel template download started...', 'info');
		window.location.href = '/dms/api/bulkupload/template';

	});

	$('#uploadMetadata').click(function(e) {
		e.preventDefault();
		const input = document.createElement('input');
		input.type = 'file';
		input.accept = '.xlsx,.xls';
		input.onchange = function(event) {
			const file = event.target.files[0];
			if (file) {
				const fileName = file.name.toLowerCase();
				const fileExtension = fileName.split('.').pop();

				if (fileExtension !== 'xlsx' && fileExtension !== 'xls') {
					showNotification('Only Excel files (.xlsx, .xls) are allowed for metadata.', 'error');
					return;
				}

				showUploadedFile('metadata', file.name);
				uploadedMetadataFile = file;
			}
		};
		input.click();
	});

	$('#uploadZipFile').click(function(e) {
		e.preventDefault();
		const input = document.createElement('input');
		input.type = 'file';
		input.accept = '.zip';
		input.onchange = function(event) {
			const file = event.target.files[0];
			if (file) {
				const fileName = file.name.toLowerCase();
				const fileExtension = fileName.split('.').pop();

				if (fileExtension !== 'zip') {
					showNotification('Only ZIP files are allowed.', 'error');
					return;
				}

				showUploadedFile('zip', file.name);
				uploadedZipFile = file;
			}
		};
		input.click();
	});




	$('#previewMetadata').click(async function() {
		const formData = new FormData();
		formData.append('file', uploadedMetadataFile);

		try {
			// Step 1: Upload metadata file and get response
			const response = await $.ajax({
				url: '/dms/api/bulkupload/metadata/upload',
				type: 'POST',
				data: formData,
				processData: false,
				contentType: false,
			});

			// Step 2: Group rows
			const groupedRows = [];
			let currentGroup = [];
			for (let i = 0; i < response.length; i++) {
				const entry = response[i];
				if (Object.keys(entry).length === 0 && currentGroup.length > 0) {
					groupedRows.push(currentGroup);
					currentGroup = [];
				} else if (Object.keys(entry).length > 0) {
					currentGroup.push(entry);
				}
			}
			if (currentGroup.length > 0) groupedRows.push(currentGroup);

			// Step 3: Build headers
			const firstRow = groupedRows[0];
			const headers = firstRow.map(field => Object.keys(field)[0]);
			let headerHtml = headers.map(h => `<th>${h}*</th>`).join('');
			$("#tableHeaderRow").html(headerHtml);

			// Step 4: Preload dropdowns (folders, departments, statuses)
			const [folders, departments, statuses] = await Promise.all([
				$.get("/dms/api/folders/get"),
				$.get("/dms/api/departments/get"),
				$.get("/dms/api/statuses/get"),
			]);

			// Step 5: Build select helper
			function buildSelect(id, name, selectedValue, optionsArray, className = "") {
				let html = `<select name="${name}" id="${name.toLowerCase().replace(/\s+/g, '')}_${id}" class="${className}" required>`;
				html += `<option value="">--Select--</option>`;
				optionsArray.forEach(opt => {
					let selected = opt.name === selectedValue ? 'selected' : '';
					html += `<option value="${opt.id}" ${selected}>${opt.name}</option>`;
				});
				html += `</select>`;
				return html;
			}

			// Step 6: Build table body
			let bodyHtml = "";
			groupedRows.forEach((group, index) => {
				bodyHtml += "<tr>";
				group.forEach(field => {
					const fieldName = Object.keys(field)[0];
					const { value, errorMessage } = field[fieldName];
					let cellContent = "";
					let isValid = value && (!errorMessage || errorMessage.trim() === "");
					let validClass = isValid ? "is-valid" : "is-invalid";
					if (["Folder", "Department", "Current Status"].includes(fieldName)) {
						let options = [];
						if (fieldName === "Folder") {
							cellContent += buildSelect(index, fieldName, value, folders, `preview_folder ${validClass} ${fieldName.toLowerCase().replace(/\s+/g, '')}`);
						} else if (fieldName === "Department") {
							cellContent += buildSelect(index, fieldName, value, departments, `${validClass} ${fieldName.toLowerCase().replace(/\s+/g, '')}`);
						} else if (fieldName === "Current Status") {
							cellContent += buildSelect(index, fieldName, value, statuses, `${validClass} ${fieldName.toLowerCase().replace(/\s+/g, '')}`);
						}
					} else if (fieldName === "Sub-Folder") {
						// Add a data attribute with the subfolder name
						cellContent += `<select class="${validClass} ${fieldName.toLowerCase().replace(/\s+/g, '')}" name="${fieldName}" id="sub-folder_${index}" data-selected-subfolder-name="${value}" required>
								<option value="">--Select--</option>
							</select>`;
					} else if (fieldName === "Revision Date") {
						// Add a data attribute with the subfolder name
						cellContent += `<input type="date" class="${validClass} ${fieldName.toLowerCase().replace(/\s+/g, '')}" name="${fieldName}" value="${value}" id="${fieldName.toLowerCase().replace(/\s+/g, '')}_${index}" required/>`;
					}
					else {
						cellContent += `<input type="text" class="${validClass} ${fieldName.toLowerCase().replace(/\s+/g, '')}" name="${fieldName}" value="${value}" id="${fieldName.toLowerCase().replace(/\s+/g, '')}_${index}" required/>`;
					}

					//if (errorMessage && errorMessage.trim() !== "") {
					const errorMessageId = fieldName.toLowerCase().replace(/\s+/g, '') + "_errormessage_" + index;
					cellContent += `<div style="color: red; font-size: 12px;" id="${errorMessageId.trim()}">${errorMessage}</div>`;
					//cellContent += `<div class="error-message">This field is required</div>`

					bodyHtml += `<td>${cellContent}</td>`;
				});
				bodyHtml += "</tr>";
			});
			$("#tableBody").html(bodyHtml);
			$('.preview_folder').each(function() {
				const val = $(this).val();
				if (val) {
					$(this).trigger('change');
				}
			});
			showPreviewMetadata();

		} catch (xhr) {
			alert("Upload failed: " + xhr.responseText);
		}
	});

	const rowValidatorFunction = function() {
		const input = $(this);
		const inputId = input.attr('id');

		const fieldName = inputId.split('_')[0];
		const index = inputId.split('_')[1];

		const filenameId = "#filename_" + index;
		const fileNameValue = $(filenameId).val().trim();

		const fileNumberId = "#filenumber_" + index;
		const fileNumberValue = $(fileNumberId).val().trim();

		const revisionNoId = "#revisionno_" + index;
		const revisionNoValue = $(revisionNoId).val().trim();

		const revisionDateId = "#revisiondate_" + index;
		const revisionDateValue = $(revisionDateId).val().trim();

		const folderId = "#folder_" + index;
		const folderValue = $(folderId + " option:selected").text().trim();

		const subfolderId = "#sub-folder_" + index;
		const subfolderValue = $(subfolderId + " option:selected").text().trim();

		const departmentId = "#department_" + index;
		const departmentValue = $(departmentId + " option:selected").text().trim();

		const statusId = "#currentstatus_" + index;
		const statusValue = $(statusId + " option:selected").text().trim();

		const uploadDocId = "#uploaddocument_" + index;
		const uploadDocValue = $(uploadDocId).val().trim();


		//const formData = new FormData();

		// Example 2D array
		const rows = [
			["File Name", "File Number", "Revision No", "Revision Date", "Folder", "Sub-Folder", "Department", "Current Status", "Upload Document"],
			[fileNameValue, fileNumberValue, revisionNoValue, revisionDateValue, folderValue, subfolderValue, departmentValue, statusValue, uploadDocValue]
		];

		// Convert to JSON string and append
		//formData.append('rows', );

		$.ajax({
			url: '/dms/api/bulkupload/metadata/validate',  // Change to your actual endpoint
			type: 'POST',
			data: JSON.stringify(rows),
			processData: false,
			contentType: 'application/json',
			success: function(response) {
				//const rowIndex = 0; // Update this as needed (0 for first row, 1 for second, etc.)

				response.forEach((fieldObj, i) => {
					if (Object.keys(fieldObj).length === 0) return; // Skip empty objects

					const fieldName = Object.keys(fieldObj)[0]; // e.g., "File Name"
					const { errorMessage } = fieldObj[fieldName];

					const fieldKey = fieldName.toLowerCase().replace(/\s+/g, ''); // e.g., "File Name" ➜ "filename"
					const errorDivId = `#${fieldKey}_errormessage_${index}`;

					$(errorDivId).text(errorMessage || '');
					var fieldId = "#" + fieldKey + "_" + index; // Update the text (empty if no error)
					if (errorMessage !== "") {
						$(fieldId).removeClass('is-valid').addClass('is-invalid');
					} else {
						$(fieldId).removeClass('is-invalid').addClass('is-valid');
					}
				});
			}
		});


	};

	// Validations of file name
	$(document).on('change', '.filename', rowValidatorFunction);
	// Validations of file number
	$(document).on('change', '.filenumber', rowValidatorFunction);
	// Validations of revisionno
	$(document).on('change', '.revisionno', rowValidatorFunction);
	// Validations of revisiondate
	$(document).on('change', '.revisiondate', rowValidatorFunction);
	// Validations of folder
	$(document).on('change', '.preview_folder', function() {
		const elementId = $(this).attr('id');           // e.g. "folder_3"
		const index = elementId.split('_')[1];          // Get "3"
		const folderId = $(this).val();                 // Selected folder ID
		const subfolderSelectId = `#sub-folder_${index}`;
		const folderErrorMsgId = `#folder_errormessage_${index}`;

		$(subfolderSelectId).empty().append('<option value="">--Select--</option>');
		$(subfolderSelectId).removeClass('is-valid').addClass('is-invalid');
		if (!folderId) {
			$(this).removeClass('is-valid').addClass('is-invalid');
			return;
		}
		$(folderErrorMsgId).text('');
		$(this).removeClass('is-invalid').addClass('is-valid');

		$.ajax({
			url: `/dms/api/subfolders/${folderId}`,
			type: "GET",
			success: function(data) {
				if (Array.isArray(data)) {
					let selectedSubfolderName = $(`#sub-folder_${index}`).data('selected-subfolder-name'); // from metadata

					data.forEach(sub => {
						let selected = selectedSubfolderName && sub.name === selectedSubfolderName ? 'selected' : '';
						$(subfolderSelectId).append(
							$('<option>', {
								value: sub.id,
								text: sub.name,
								selected: selected
							})
						);
					});
					if ($(subfolderSelectId).val() && $(subfolderSelectId).val() !== "") {
						isAnySubFolderSelected = true;
						//return false; // exit loop early
					}

					if (isAnySubFolderSelected !== '') {
						// set error message to blank
						$(`#sub-folder_errormessage_${index}`).text('');
						$(subfolderSelectId).removeClass('is-invalid').addClass('is-valid');
					} else {
						$(subfolderSelectId).removeClass('is-valid').addClass('is-invalid');
					}
				}
			},
			error: function() {
				console.error("Failed to load subfolders");
			}
		});


	});
	//$(document).on('change', '.folder',rowValidatorFunction);
	// Validations of Sub Folder
	$(document).on('change', '.sub-folder', rowValidatorFunction);
	// Validations of file number
	$(document).on('change', '.department', rowValidatorFunction);
	// Validations of file number
	$(document).on('change', '.currentstatus', rowValidatorFunction);
	//




	// Bulk update link handlers
	$('#downloadUpdateTemplate').click(function(e) {
		e.preventDefault();
		showNotification('Excel template for bulk update download started...', 'info');
	});

	$('#uploadUpdateMetadata').click(function(e) {
		e.preventDefault();
		const input = document.createElement('input');
		input.type = 'file';
		input.accept = '.xlsx,.xls';
		input.id = 'updateMetadataInput';
		input.onchange = function(event) {
			const file = event.target.files[0];
			if (file) {
				showUpdateUploadedFile('update-metadata', file.name);
				updateUploadedMetadataFile = file;
			}
		};
		input.click();
	});

	$('#uploadUpdateZipFile').click(function(e) {
		e.preventDefault();
		const input = document.createElement('input');
		input.type = 'file';
		input.accept = '.zip';
		input.onchange = function(event) {
			const file = event.target.files[0];
			if (file) {
				showUpdateUploadedFile('update-zip', file.name);
				updateUploadedZipFile = file;
			}
		};
		input.click();
	});

	$('#previewUpdateMetadata').click(function() {
		showNotification('Preview update metadata functionality', 'info');
	});

	// Remove uploaded file handlers
	$(document).on('click', '.remove-file-btn', function() {
		const fileType = $(this).data('type');
		if (fileType.startsWith('update-')) {
			removeUpdateUploadedFile(fileType);
		} else {
			removeUploadedFile(fileType);
		}
	});

	// Preview metadata modal handlers - FIXED VERSION
	$('#cancelPreview').on('click', function() {
		$('#previewModal').css('display', 'none');
		$('#previewModal input, #previewModal select').removeClass('error-field success-field');
		$('#previewModal .error-message').removeClass('show');
	});

	$('#previewModal').on('click', function(e) {
		if ($(e.target).is('#previewModal')) {
			$('#previewModal').css('display', 'none');
			$('#previewModal input, #previewModal select').removeClass('error-field success-field');
			$('#previewModal .error-message').removeClass('show');
		}
	});

	$('#savePreview').on('click', function() {
		// Validate required fields in the preview modal
		let isValid = true;
		let inputRows = [];
		let prevIndex = "0";
		let map = {};
		$('#previewModal input[required], #previewModal select[required]').each(function() {

			const inputId = $(this).attr('id');
			const fieldName = inputId.split('_')[0];
			const index = inputId.split('_')[1];
			if (index !== prevIndex) {
				inputRows.push(map);
				map = {};
			}
			const errorMsgId = `#${fieldName}_errormessage_${index}`
			const errorMsg = $(errorMsgId).text();
			if (!$(this).val().trim() || errorMsg !== "") {
				//$(this).addClass('error-field');
				//$(this).siblings('.error-message').addClass('show');
				isValid = false;
			} else { // form is valid than persist at backend
				if (fieldName === 'sub-folder') {
					map['subfolder'] = $(this).val();
				} else {
					map[fieldName] = $(this).val();
				}//inputRows.push(map);
				//$(this).removeClass('error-field');
				//$(this).siblings('.error-message').removeClass('show');
			}
			prevIndex = index;
		});
		inputRows.push(map);

		if (!isValid) return;
		// Send to backend
		$.ajax({
		    url: '/dms/api/bulkupload/metadata/save',
		    type: 'POST',
		    contentType: 'application/json',
		    data: JSON.stringify(inputRows), // 👈 Convert list to JSON string
		    success: function(response) {
				localStorage.setItem('uploadedMetaDataId', response);
				$('#previewModal').css('display', 'none');
				$('#previewModal input, #previewModal select').removeClass('error-field success-field');
				$('#previewModal .error-message').removeClass('show');
				showNotification('Metadata saved successfully!', 'success');
		    },
		    error: function(xhr) {
		        console.error('Error:', xhr.responseText);
		        alert('Error saving metadata: ' + xhr.responseText);
		    }
		});
	});

	// File input change handlers
	$('#singleFileInput').change(function() {
		const files = this.files;
		if (files.length > 0) {
			$(this).removeClass('error-field').addClass('success-field');
			$(this).siblings('.error-message').removeClass('show');
		} else {
			$(this).addClass('error-field');
			$(this).siblings('.error-message').addClass('show');
		}
	});

	$('#updateDocumentFile').change(function() {
		const file = this.files[0];
		if (file) {
			console.log('Update file selected:', file.name);
		}
	});

	// HELPER FUNCTIONS

	function handleContextMenuAction(action, targetCell) {
		const fileName = targetCell.val() || targetCell.text();
		const row = targetCell.closest('tr');

		switch (action) {
			case 'send':
				const fileType = row.find('.file-type-label').text();
				const fileNumber = row.find('td:nth-child(2) input').val() || row.find('td:nth-child(2)').text();
				const revisionNo = row.find('td:nth-child(4) input').val() || row.find('td:nth-child(4)').text();
				const documentType = row.find('td:nth-child(6) input').val() || row.find('td:nth-child(6)').text();
				const folder = row.find('td:nth-child(7) input').val() || row.find('td:nth-child(7)').text();
				const subFolder = row.find('td:nth-child(8) input').val() || row.find('td:nth-child(8)').text();

				selectedDocument = {
					fileName: fileName,
					fileType: fileType,
					fileNumber: fileNumber,
					revisionNo: revisionNo,
					documentType: documentType,
					folder: folder,
					subFolder: subFolder
				};

				showSendDocumentsModal();
				break;
			case 'update':
				const updateData = {
					fileName: fileName,
					fileType: row.find('.file-type-label').text(),
					fileNumber: row.find('td:nth-child(2) input').val() || row.find('td:nth-child(2)').text(),
					revisionNo: row.find('td:nth-child(4) input').val() || row.find('td:nth-child(4)').text(),
					status: row.find('td:nth-child(5) input').val() || row.find('td:nth-child(5)').text(),
					documentType: row.find('td:nth-child(6) input').val() || row.find('td:nth-child(6)').text(),
					folder: row.find('td:nth-child(7) input').val() || row.find('td:nth-child(7)').text(),
					subFolder: row.find('td:nth-child(8) input').val() || row.find('td:nth-child(8)').text(),
					createdBy: row.find('td:nth-child(9) input').val() || row.find('td:nth-child(9)').text(),
					dateUploaded: row.find('td:nth-child(10) input').val() || row.find('td:nth-child(10)').text(),
					revisionDate: row.find('td:nth-child(11) input').val() || row.find('td:nth-child(11)').text(),
					department: row.find('td:nth-child(12) input').val() || row.find('td:nth-child(12)').text()
				};

				selectedDocument = updateData;
				showUpdateDocumentsModal();
				break;
			case 'view-old':
				showNotification('Viewing old versions: ' + fileName, 'info');
				break;
			case 'not-required':
				showNotification('Marked as not required: ' + fileName, 'info');
				break;
			case 'download':
				showNotification('Downloading: ' + fileName, 'info');
				break;
			case 'print':
				showNotification('Printing: ' + fileName, 'info');
				break;
		}
	}

	function handleSingleUpload() {
		if (!validateForm('#singleUploadTab')) {
			return;
		}

		const files = $('#singleFileInput')[0].files;
		if (files.length === 0) {
			$('#singleFileInput').addClass('error-field');
			$('#singleFileInput').siblings('.error-message').addClass('show');
			return;
		}

		const formData = {
			fileName: $('#fileName').val().trim(),
			fileNumber: $('#fileNumber').val().trim(),
			revisionNo: $('#revisionNo').val(),
			revisionDate: $('#revisionDate').val(),
			folder: $('#folder').val(),
			subFolder: $('#subFolder').val(),
			department: $('#department').val(),
			status: $('#currentStatus').val()
		};

		// Check for duplicate file numbers
		let duplicateFound = false;
		if (mainTableInstance) {
			mainTableInstance.rows().every(function() {
				const rowData = this.node();
				const existingNumber = $(rowData).find('td:nth-child(2) input').val();
				if (existingNumber === formData.fileNumber) {
					duplicateFound = true;
					return false;
				}
			});
		}

		if (duplicateFound) {
			if (!confirm('A document with this file number already exists. Do you want to continue?')) {
				return;
			}
		}

		// Process files and add to table
		for (let i = 0; i < files.length; i++) {
			addFileToTable(files[i], formData);
		}

		showNotification(`Successfully uploaded ${files.length} file(s)`, 'success');
		$('#uploadModal').hide();
		resetUploadForm();
	}

	function handleBulkUpload() {
		if (!uploadedMetadataFile || !uploadedZipFile) {
			showNotification('Please upload both metadata file and ZIP file for bulk upload.', 'error');
			return;
		}

		showNotification('Processing bulk upload...', 'info');
		$('#uploadModal').hide();
		resetUploadForm();
	}

	function addFileToTable(file, metadata = {}) {
		const fileExtension = file.name.split('.').pop().toLowerCase();
		const fileTypeClass = getFileTypeClass(fileExtension);
		const currentDate = getCurrentDateString();
		const currentUser = 'Mansi';

		const fileName = metadata.fileName || file.name;
		const fileNumber = metadata.fileNumber || '';
		const revisionNo = metadata.revisionNo || 'R01';
		const status = metadata.status || 'Draft';
		const department = metadata.department || '';
		const folder = metadata.folder || '';
		const subFolder = metadata.subFolder || '';

		const newRowData = [
			`<span class="file-type-label ${fileTypeClass} file-name-cell">${fileExtension.toUpperCase()}</span>`,
			`<input type="text" class="editable-input file-name-cell" value="${fileNumber}">`,
			`<input type="text" class="editable-input file-name-cell" value="${fileName}">`,
			`<input type="text" class="editable-input file-name-cell" value="${revisionNo}">`,
			`<input type="text" class="editable-input file-name-cell" value="${status}">`,
			`<input type="text" class="editable-input file-name-cell" value="${metadata.documentType || ''}">`,
			`<input type="text" class="editable-input file-name-cell" value="${folder}">`,
			`<input type="text" class="editable-input file-name-cell" value="${subFolder}">`,
			`<input type="text" class="editable-input file-name-cell" value="${currentUser}">`,
			`<input type="text" class="editable-input file-name-cell" value="${currentDate}">`,
			`<input type="text" class="editable-input file-name-cell" value="${formatDateForDisplay(metadata.revisionDate) || currentDate}">`,
			`<input type="text" class="editable-input file-name-cell" value="${department}">`,
			`<input type="text" class="editable-input file-name-cell" value="">`
		];

		if (mainTableInstance) {
			mainTableInstance.row.add(newRowData).draw();
		}
	}

	function resetUploadForm() {
		$('#uploadModal .error-field').removeClass('error-field success-field');
		$('#uploadModal .error-message').removeClass('show');

		$('#fileName, #fileNumber').val('');
		$('#folder, #subFolder, #department, #currentStatus').val('');
		$('#singleFileInput').val('');
		$('#revisionDate').val('');
		$('#revisionNo').val('R01');

		resetBulkUploadForm();
		uploadedMetadataFile = null;
		uploadedZipFile = null;

		$('#uploadModal .upload-tab').removeClass('active');
		$('#uploadModal .upload-tab[data-tab="single"]').addClass('active');
		$('.single-upload-options, .bulk-upload-options').removeClass('active');
		$('#singleUploadTab').addClass('active');
	}

	function showSendDocumentsModal() {
		resetSendForm();

		if (selectedDocument) {
			const attachmentItem = `
                        <div class="attachment-item">
                            <span>${selectedDocument.fileName} (${selectedDocument.fileType})</span>
                            <button type="button" onclick="removeAttachment(this)" style="background: #e53e3e; color: white; border: none; border-radius: 50%; width: 20px; height: 20px; cursor: pointer;">×</button>
                        </div>
                    `;
			$('#attachmentsList').html(attachmentItem);
		}

		$('#sendDocumentsModal').css('display', 'flex');
	}

	function showUpdateDocumentsModal() {
		resetUpdateForm();

		$('#updateDocumentsModal .upload-tab').removeClass('active');
		$('#updateDocumentsModal .upload-tab[data-tab="single"]').addClass('active');
		$('.single-update-options, .bulk-update-options').removeClass('active');
		$('#singleUpdateTab').addClass('active');

		if (selectedDocument) {
			$('#updateFileName').val(selectedDocument.fileName);
			$('#updateFileNumber').val(selectedDocument.fileNumber);
			$('#updateRevisionNo').val(selectedDocument.revisionNo);

			if (selectedDocument.revisionDate) {
				const dateParts = selectedDocument.revisionDate.split('.');
				if (dateParts.length === 3) {
					const formattedDate = `${dateParts[2]}-${dateParts[1].padStart(2, '0')}-${dateParts[0].padStart(2, '0')}`;
					$('#updateRevisionDate').val(formattedDate);
				}
			}

			$('#updateCurrentStatus').val(selectedDocument.status);
			$('#updateDepartment').val(selectedDocument.department);
			$('#updateFolder').val(selectedDocument.folder);

			// Update sub-folder options and set value
			updateSubFolderOptions('#updateFolder', '#updateSubFolder');
			setTimeout(() => {
				$('#updateSubFolder').val(selectedDocument.subFolder);
			}, 100);

			const currentDocumentItem = `
                        <div class="current-document-item">
                            <div class="document-info">
                                <div class="document-name">${selectedDocument.fileName}</div>
                                <div class="document-details">
                                    File Number: ${selectedDocument.fileNumber} | 
                                    Revision: ${selectedDocument.revisionNo} | 
                                    Status: ${selectedDocument.status} | 
                                    Department: ${selectedDocument.department} |
                                    Folder: ${selectedDocument.folder} |
                                    Sub-Folder: ${selectedDocument.subFolder}
                                </div>
                            </div>
                            <span class="file-type-badge">${selectedDocument.fileType}</span>
                        </div>
                    `;
			$('#currentDocumentInfo').html(currentDocumentItem);
		}

		$('#updateDocumentsModal').css('display', 'flex');
	}

	function resetSendForm() {
		$('#sendDocumentsModal .error-field').removeClass('error-field success-field');
		$('#sendDocumentsModal .error-message').removeClass('show');

		$('#sendTo, #sendCc, #sendSubject, #sendReason').val('');
		$('#responseExpected').val('');
		$('#targetResponseDate').val('');
		$('#attachmentsList').empty();
	}

	function resetUpdateForm() {
		$('#updateDocumentsModal .error-field').removeClass('error-field success-field');
		$('#updateDocumentsModal .error-message').removeClass('show');

		$('#updateFileName, #updateFileNumber, #updateRevisionNo, #updateReason').val('');
		$('#updateRevisionDate').val('');
		$('#updateFolder, #updateSubFolder, #updateDepartment, #updateCurrentStatus').val('');
		$('#updateDocumentFile').val('');
		$('#currentDocumentInfo').empty();

		resetBulkUpdateForm();
		updateUploadedMetadataFile = null;
		updateUploadedZipFile = null;
	}



	function showPreviewMetadata() {
		$('#previewFileName, #previewFileNumber').val('');
		$('#previewFolder, #previewSubFolder, #previewDepartment, #previewCurrentStatus').val('');
		$('#previewRevisionDate').val('');

		//$('#previewModal input, #previewModal select').addClass('error-field');
		$('#previewModal .error-message').addClass('show');

		$('#previewModal').css('display', 'flex');
	}

	function showUploadedFile(fileType, fileName) {
		if (fileType === 'metadata') {
			$('#metadataFileName').text(fileName);
			$('#metadataStatus').show();
		} else if (fileType === 'zip') {
			$('#zipFileName').text(fileName);
			$('#zipStatus').show();
		}
	}

	function removeUploadedFile(fileType) {
		if (fileType === 'metadata') {
			$('#metadataStatus').hide();
			$('#metadataFileName').text('');
			uploadedMetadataFile = null;
		} else if (fileType === 'zip') {
			$('#zipStatus').hide();
			$('#zipFileName').text('');
			uploadedZipFile = null;
		}
	}

	function showUpdateUploadedFile(fileType, fileName) {
		if (fileType === 'update-metadata') {
			$('#updateMetadataFileName').text(fileName);
			$('#updateMetadataStatus').show();
		} else if (fileType === 'update-zip') {
			$('#updateZipFileName').text(fileName);
			$('#updateZipStatus').show();
		}
	}

	function removeUpdateUploadedFile(fileType) {
		if (fileType === 'update-metadata') {
			$('#updateMetadataStatus').hide();
			$('#updateMetadataFileName').text('');
			updateUploadedMetadataFile = null;
		} else if (fileType === 'update-zip') {
			$('#updateZipStatus').hide();
			$('#updateZipFileName').text('');
			updateUploadedZipFile = null;
		}
	}

	function resetBulkUploadForm() {
		$('#metadataStatus, #zipStatus').hide();
		$('#metadataFileName, #zipFileName').text('');
		uploadedMetadataFile = null;
		uploadedZipFile = null;
	}

	// Utility functions
	function isValidEmail(email) {
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		return emailRegex.test(email);
	}

	function getFileTypeClass(extension) {
		switch (extension) {
			case 'pdf': return 'file-type-pdf';
			case 'doc':
			case 'docx': return 'file-type-word';
			case 'xls':
			case 'xlsx': return 'file-type-excel';
			case 'dwg': return 'file-type-dwg';
			case 'jpg':
			case 'jpeg': return 'file-type-jpeg';
			default: return 'file-type-pdf';
		}
	}

	function getCurrentDateString() {
		const now = new Date();
		const day = String(now.getDate()).padStart(2, '0');
		const month = String(now.getMonth() + 1).padStart(2, '0');
		const year = now.getFullYear();
		return `${day}.${month}.${year}`;
	}

	function formatDateForDisplay(dateString) {
		if (dateString && dateString.includes('-')) {
			const parts = dateString.split('-');
			if (parts.length === 3) {
				return `${parts[2].padStart(2, '0')}.${parts[1].padStart(2, '0')}.${parts[0]}`;
			}
		}
		return dateString;
	}

	// Global function for removing attachments (called from HTML)
	window.removeAttachment = function(button) {
		$(button).parent().remove();
	};

	// Navigation functionality
	document.querySelectorAll('.sidebar-item').forEach(item => {
		item.addEventListener('click', function() {
			const targetPage = this.getAttribute('data-target');
			if (targetPage) {
				window.location.href = targetPage;
			}
		});
	});

	document.querySelectorAll('.sidebar-header[data-target]').forEach(item => {
		item.addEventListener('click', function() {
			const target = this.getAttribute('data-target');
			if (target) {
				window.location.href = target;
			}
		});
	});

	// Enhanced Column Filter System
	function getColumnData(columnIndex) {
		const uniqueValues = new Set();

		$('#mainTable tbody tr:visible').each(function() {
			const cell = $(this).find('td').eq(columnIndex);
			let value = '';

			const input = cell.find('input, select');
			if (input.length) {
				value = input.val() || input.text();
			} else {
				value = cell.text().trim();
			}

			if (value && value !== '') {
				uniqueValues.add(value);
			}
		});

		return Array.from(uniqueValues).sort();
	}

	function createFilterDropdown(columnIndex) {
		const dropdown = $(`.filter-dropdown[data-column="${columnIndex}"]`);
		dropdown.empty();

		dropdown.append(`
                    <input type="text" class="filter-search" placeholder="Search options...">
                    <div class="filter-options"></div>
                `);

		const uniqueValues = new Set();
		$('#mainTable tbody tr').each(function() {
			const cell = $(this).find('td').eq(columnIndex);
			let value = cell.find('input').length ? cell.find('input').val() : cell.text();
			value = value.trim();
			if (value) uniqueValues.add(value);
		});

		const optionsContainer = dropdown.find('.filter-options');
		Array.from(uniqueValues).sort().forEach((value, idx) => {
			const checked = (columnFilters[columnIndex] || []).includes(value) ? 'checked' : '';
			optionsContainer.append(`
                        <div class="filter-option">
                            <input type="checkbox" class="filter-checkbox" data-column="${columnIndex}" value="${value}" id="filter${columnIndex}_${idx}" ${checked}>
                            <label for="filter${columnIndex}_${idx}">${value}</label>
                        </div>
                    `);
		});
	}

	// Toggle dropdown on ▼ click
	$(document).on('click', '.filter-dropdown-toggle', function(e) {
		e.stopPropagation();
		const container = $(this).closest('.filter-container');
		const dropdown = container.find('.filter-dropdown');
		$('.filter-dropdown').not(dropdown).removeClass('show');
		dropdown.toggleClass('show');
		const columnIndex = $(this).siblings('.column-filter').data('column');
		createFilterDropdown(columnIndex);
	});

	// Hide dropdown when clicking outside
	$(document).on('click', function() {
		$('.filter-dropdown').removeClass('show');
	});

	// Prevent dropdown from closing when clicking inside
	$(document).on('click', '.filter-dropdown', function(e) {
		e.stopPropagation();
	});

	// Handle checkbox change
	$(document).on('change', '.filter-checkbox', function() {
		const columnIndex = $(this).data('column');
		columnFilters[columnIndex] = [];
		$(`.filter-dropdown[data-column="${columnIndex}"] .filter-checkbox:checked`).each(function() {
			columnFilters[columnIndex].push($(this).val());
		});
		updateFilterInput(columnIndex);
		applyColumnFilters();
	});

	// Update filter input display
	function updateFilterInput(columnIndex) {
		const input = $(`.column-filter[data-column="${columnIndex}"]`);
		const selected = columnFilters[columnIndex] || [];
		if (selected.length === 0) {
			input.val('');
		} else if (selected.length === 1) {
			input.val(selected[0]);
		} else {
			input.val(`${selected.length} selected`);
		}
	}

	// Filter table rows
	function applyColumnFilters() {
		$('#mainTable tbody tr').each(function() {
			let show = true;
			$(this).find('td').each(function(colIdx) {
				if (columnFilters[colIdx] && columnFilters[colIdx].length > 0) {
					let cellValue = $(this).find('input').length ? $(this).find('input').val() : $(this).text();
					cellValue = cellValue.trim();
					if (!columnFilters[colIdx].includes(cellValue)) {
						show = false;
					}
				}
			});
			$(this).toggle(show);
		});
	}

	function updateAllColumnFilters() {
		// This function can be called when table is redrawn
	}

	// Prevent typing in filter input
	$(document).on('keydown', '.column-filter', function(e) {
		e.preventDefault();
	});

	// Filter dropdown options as user types in the search box
	$(document).on('input', '.filter-search', function() {
		const searchTerm = $(this).val().toLowerCase();
		const options = $(this).siblings('.filter-options').find('.filter-option');
		options.each(function() {
			const label = $(this).find('label').text().toLowerCase();
			$(this).toggle(label.includes(searchTerm));
		});
	});

	// Right-click or context menu logic should store the row
	$('#mainTable').on('contextmenu', 'tr', function(e) {
		e.preventDefault();
		currentUpdateRow = $(this);
		$('#contextMenu').css({ top: e.pageY, left: e.pageX, display: 'block' });
		$('#contextMenu').data('row', currentUpdateRow);
	});

	// When clicking "Update" in the context menu
	$(document).on('click', '.context-menu-item[data-action="update"]', function() {
		const $row = $('#contextMenu').data('row');
		if (!$row) return;
		currentUpdateRow = $row;

		// Autofill form fields from table row with updated column positions
		$('#updateFileName').val($row.find('td').eq(2).find('input').val());
		$('#updateFileNumber').val($row.find('td').eq(1).find('input').val());
		$('#updateRevisionNo').val($row.find('td').eq(3).find('input').val());
		$('#updateRevisionDate').val($row.find('td').eq(10).find('input').val());
		$('#updateFolder').val($row.find('td').eq(6).find('input').val());
		$('#updateDepartment').val($row.find('td').eq(11).find('input').val());
		$('#updateCurrentStatus').val($row.find('td').eq(4).find('input').val());

		// Update sub-folder options and set value
		updateSubFolderOptions('#updateFolder', '#updateSubFolder');
		setTimeout(() => {
			$('#updateSubFolder').val($row.find('td').eq(7).find('input').val());
		}, 100);

		$('#updateDocumentsModal').addClass('show');
		$('#contextMenu').hide();
	});

	$('#previewUpdateMetadata').on('click', function() {
		if (!$('#updateMetadataFileName').text()) {
			alert('Please upload a metadata file before preview.');
			return;
		}
		showPreviewMetadata();
	});

	$('#uploadBtn').on('click', function() {
		// inititialize department dropdown
		$.ajax({
			url: '/dms/api/departments/get',  // Replace with your actual API endpoint
			method: 'GET',
			success: function(data) {
				// Clear previous options except the default
				$('#department').find('option:not(:first)').remove();

				// Append new options
				$.each(data, function(index, department) {
					$('#department').append(
						$('<option>', {
							value: department.id,
							text: department.name
						})
					);
				});
			},
			error: function(xhr) {
				console.error('Failed to load departments:', xhr.responseText);
			}
		});

		// inititialize status dropdown
		$.ajax({
			url: '/dms/api/statuses/get',  // Replace with your actual API endpoint
			method: 'GET',
			success: function(data) {
				// Clear previous options except the default
				$('#currentStatus').find('option:not(:first)').remove();

				// Append new options
				$.each(data, function(index, status) {
					$('#currentStatus').append(
						$('<option>', {
							value: status.id,
							text: status.name
						})
					);
				});
			},
			error: function(xhr) {
				console.error('Failed to load departments:', xhr.responseText);
			}
		});
		// inititialize folder dropdown
		$.ajax({
			url: '/dms/api/folders/get',  // Replace with your actual API endpoint
			method: 'GET',
			success: function(data) {
				// Clear previous options except the default
				$('#folder').find('option:not(:first)').remove();

				// Append new options
				$.each(data, function(index, folder) {
					$('#folder').append(
						$('<option>', {
							value: folder.id,
							text: folder.name
						})
					);
				});
			},
			error: function(xhr) {
				console.error('Failed to load departments:', xhr.responseText);
			}
		});
	});



});