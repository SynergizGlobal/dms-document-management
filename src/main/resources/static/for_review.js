$(document).ready(function () {
    // Global variables
    let selectedDocument = null;
    let uploadedMetadataFile = null;
    let uploadedZipFile = null;
    let updateUploadedMetadataFile = null;
    let updateUploadedZipFile = null;

    // Sidebar functionality
    $('.sidebar-header').click(function () {
        $(this).toggleClass('expanded');
    });

    $('.sidebar-item').click(function () {
        $('.sidebar-item').removeClass('active');
        $(this).addClass('active');
    });

    // Upload button click - show modal
    $('#uploadBtn').click(function (e) {
        e.preventDefault();
        $('#uploadModal').css('display', 'flex');
    });

    // Upload modal tabs (only for upload modal)
    $('#uploadModal .upload-tab').click(function () {
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

    // Update modal tabs (only for update modal)
    $('#updateDocumentsModal .upload-tab').click(function () {
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
    $('#cancelUpload').click(function () {
        $('#uploadModal').hide();
        resetUploadForm();
    });

    // Close modal when clicking outside - Upload
    $('#uploadModal').click(function (e) {
        if (e.target === this) {
            $(this).hide();
            resetUploadForm();
        }
    });

    // Save upload
    $('#saveUpload').click(function () {
        const activeTab = $('#uploadModal .upload-tab.active').data('tab');
        if (activeTab === 'single') {
            handleSingleUpload();
        } else {
            handleBulkUpload();
        }
    });

    // Right-click context menu for file name cells
    $(document).on('contextmenu', '.file-name-cell', function (e) {
        e.preventDefault();
        const contextMenu = $('#contextMenu');

        // Position the context menu
        contextMenu.css({
            top: e.pageY + 'px',
            left: e.pageX + 'px',
            display: 'block'
        });

        // Store reference to the clicked cell
        contextMenu.data('target-cell', $(this));

        return false;
    });

    // Hide context menu on click elsewhere
    $(document).click(function () {
        $('#contextMenu').hide();
    });

    // Handle context menu item clicks
    $('.context-menu-item').click(function () {
        const action = $(this).data('action');
        const targetCell = $('#contextMenu').data('target-cell');

        handleContextMenuAction(action, targetCell);
        $('#contextMenu').hide();
    });

    // Send Documents Modal handlers
    $('#cancelSend').click(function () {
        $('#sendDocumentsModal').hide();
        resetSendForm();
    });

    $('#sendDocumentsModal').click(function (e) {
        if (e.target === this) {
            $(this).hide();
            resetSendForm();
        }
    });

    $('#sendDocument').click(function () {
        if (validateSendForm()) {
            // Process the send request here
            alert('Document sent successfully!');
            $('#sendDocumentsModal').hide();
            resetSendForm();
        } else {
            alert('Please fill in all required fields.');
        }
    });

    $('#saveDraft').click(function () {
        // Process save draft here
        alert('Draft saved successfully!');
        $('#sendDocumentsModal').hide();
        resetSendForm();
    });

    // Update Documents Modal handlers
    $('#cancelUpdate').click(function () {
        $('#updateDocumentsModal').hide();
        resetUpdateForm();
    });

    $('#updateDocumentsModal').click(function (e) {
        if (e.target === this) {
            $(this).hide();
            resetUpdateForm();
        }
    });

    $('#updateDocument').click(function () {
        const activeTab = $('#updateDocumentsModal .upload-tab.active').data('tab');
        if (activeTab === 'single') {
            if (validateUpdateForm()) {
                // Process the single update request here
                alert('Document updated successfully!');
                $('#updateDocumentsModal').hide();
                resetUpdateForm();
            } else {
                alert('Please fill in all required fields.');
            }
        } else {
            // Process bulk update
            alert('Bulk update completed successfully!');
            $('#updateDocumentsModal').hide();
            resetUpdateForm();
        }
    });

    // Bulk upload link handlers (for upload modal)
    $('#downloadTemplate').click(function (e) {
        e.preventDefault();
        alert('Downloading Excel template...');
    });

    $('#uploadMetadata').click(function (e) {
        e.preventDefault();
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.xlsx,.xls';
        input.onchange = function (event) {
            const file = event.target.files[0];
            if (file) {
                const fileName = file.name.toLowerCase();
                const fileExtension = fileName.split('.').pop();

                if (fileExtension !== 'xlsx' && fileExtension !== 'xls') {
                    alert('Error: Only Excel files (.xlsx, .xls) are allowed for metadata.');
                    return;
                }

                showUploadedFile('metadata', file.name);
            }
        };
        input.click();
    });

    $('#uploadZipFile').click(function (e) {
        e.preventDefault();
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.zip';
        input.onchange = function (event) {
            const file = event.target.files[0];
            if (file) {
                const fileName = file.name.toLowerCase();
                const fileExtension = fileName.split('.').pop();

                if (fileExtension !== 'zip') {
                    alert('Error: Only ZIP files are allowed. Please select a .zip file.');
                    return;
                }

                if (file.type && !file.type.includes('zip') && !file.type.includes('compressed')) {
                    alert('Error: Invalid file type. Only ZIP files are allowed.');
                    return;
                }

                showUploadedFile('zip', file.name);
            }
        };
        input.click();
    });

    $('#previewMetadata').click(function () {
        showPreviewMetadata();
    });

    // Bulk update link handlers (for update modal)
    $('#downloadUpdateTemplate').click(function (e) {
        e.preventDefault();
        alert('Downloading Excel template for bulk update...');
    });

    $('#uploadUpdateMetadata').click(function (e) {
        e.preventDefault();
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.xlsx,.xls';
        input.onchange = function (event) {
            const file = event.target.files[0];
            if (file) {
                const fileName = file.name.toLowerCase();
                const fileExtension = fileName.split('.').pop();

                if (fileExtension !== 'xlsx' && fileExtension !== 'xls') {
                    alert('Error: Only Excel files (.xlsx, .xls) are allowed for metadata.');
                    return;
                }

                showUpdateUploadedFile('update-metadata', file.name);
            }
        };
        input.click();
    });

    $('#uploadUpdateZipFile').click(function (e) {
        e.preventDefault();
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.zip';
        input.onchange = function (event) {
            const file = event.target.files[0];
            if (file) {
                const fileName = file.name.toLowerCase();
                const fileExtension = fileName.split('.').pop();

                if (fileExtension !== 'zip') {
                    alert('Error: Only ZIP files are allowed. Please select a .zip file.');
                    return;
                }

                showUpdateUploadedFile('update-zip', file.name);
            }
        };
        input.click();
    });

    $('#previewUpdateMetadata').click(function () {
        alert('Preview update metadata functionality');
    });

    // Remove uploaded file handlers
    $(document).on('click', '.remove-file-btn', function () {
        const fileType = $(this).data('type');
        if (fileType.startsWith('update-')) {
            removeUpdateUploadedFile(fileType);
        } else {
            removeUploadedFile(fileType);
        }
    });

    // Preview metadata modal handlers
    $('#cancelPreview').click(function () {
        $('#previewModal').hide();
    });

    $('#previewModal').click(function (e) {
        if (e.target === this) {
            $(this).hide();
        }
    });

    $('#savePreview').click(function () {
        if (validatePreviewForm()) {
            alert('Metadata saved successfully!');
            $('#previewModal').hide();
        } else {
            alert('Please fill all required fields.');
        }
    });

    // Real-time validation for preview form
    $('#previewModal input, #previewModal select').on('input change', function () {
        validatePreviewField($(this));
    });

    // File input change handler for single upload
    $('#singleFileInput').change(function() {
        const files = this.files;
        if (files.length > 0) {
            console.log('Files selected:', files.length);
        }
    });

    // File input change handler for update
    $('#updateDocumentFile').change(function() {
        const file = this.files[0];
        if (file) {
            console.log('Update file selected:', file.name);
        }
    });
});

// Updated handleContextMenuAction function
function handleContextMenuAction(action, targetCell) {
    const fileName = targetCell.val();
    const row = targetCell.closest('tr');

    switch (action) {
        case 'send':
            // Get document details from the row
            const fileType = row.find('.file-type-label').text();
            const fileNumber = row.find('td:nth-child(2) input').val();
            const revisionNo = row.find('td:nth-child(4) input').val();
            const documentType = row.find('td:nth-child(6) input').val();
            
            // Store selected document info
            selectedDocument = {
                fileName: fileName,
                fileType: fileType,
                fileNumber: fileNumber,
                revisionNo: revisionNo,
                documentType: documentType
            };

            // Show send documents modal
            showSendDocumentsModal();
            break;
        case 'update':
            // Get document details from the row for update
            const updateFileType = row.find('.file-type-label').text();
            const updateFileNumber = row.find('td:nth-child(2) input').val();
            const updateRevisionNo = row.find('td:nth-child(4) input').val();
            const status = row.find('td:nth-child(5) input').val();
            const updateDocumentType = row.find('td:nth-child(6) input').val();
            const createdBy = row.find('td:nth-child(7) input').val();
            const dateUploaded = row.find('td:nth-child(8) input').val();
            const revisionDate = row.find('td:nth-child(9) input').val();
            const department = row.find('td:nth-child(10) input').val();
            
            // Store selected document info for update
            selectedDocument = {
                fileName: fileName,
                fileType: updateFileType,
                fileNumber: updateFileNumber,
                revisionNo: updateRevisionNo,
                status: status,
                documentType: updateDocumentType,
                createdBy: createdBy,
                dateUploaded: dateUploaded,
                revisionDate: revisionDate,
                department: department
            };

            // Show update documents modal
            showUpdateDocumentsModal();
            break;
        case 'view-old':
            alert('View old versions: ' + fileName);
            break;
        case 'not-required':
            alert('Not required: ' + fileName);
            break;
        case 'download':
            alert('Download: ' + fileName);
            break;
        case 'print':
            alert('Print: ' + fileName);
            break;
    }
}

function handleSingleUpload() {
    const fileName = $('#fileName').val().trim();
    const fileNumber = $('#fileNumber').val().trim();
    const revisionDate = $('#revisionDate').val();
    const category = $('#category').val();
    const subCategory = $('#subCategory').val();
    const department = $('#department').val();
    const status = $('#currentStatus').val();
    const files = $('#singleFileInput')[0].files;

    // Validation
    const errors = [];
    if (!fileName) errors.push('File Name is required');
    if (!fileNumber) errors.push('File Number is required');
    if (!revisionDate) errors.push('Revision Date is required');
    if (!category) errors.push('Category is required');
    if (!subCategory) errors.push('Sub-Category is required');
    if (!department) errors.push('Department is required');
    if (!status) errors.push('Current Status is required');
    if (files.length === 0) errors.push('Please select at least one file');

    if (errors.length > 0) {
        alert('Please fix the following errors:\n\n' + errors.join('\n'));
        return;
    }

    // Check for duplicate file numbers
    const existingFileNumbers = [];
    $('.document-table tbody tr').each(function() {
        const existingNumber = $(this).find('td:nth-child(2) input').val().trim();
        if (existingNumber) {
            existingFileNumbers.push(existingNumber);
        }
    });

    if (existingFileNumbers.includes(fileNumber)) {
        if (!confirm('A document with this file number already exists. Do you want to continue?')) {
            return;
        }
    }

    // Process files
    for (let i = 0; i < files.length; i++) {
        addFileToTable(files[i], {
            fileName: fileName,
            fileNumber: fileNumber,
            revisionNo: $('#revisionNo').val(),
            category: category,
            subCategory: subCategory,
            department: department,
            status: status,
            revisionDate: revisionDate
        });
    }

    alert(`Successfully uploaded ${files.length} file(s)`);
    $('#uploadModal').hide();
    resetUploadForm();
}

function handleBulkUpload() {
    if (!uploadedMetadataFile || !uploadedZipFile) {
        alert('Please upload both metadata file and ZIP file for bulk upload.');
        return;
    }

    // Simulate bulk upload processing
    alert('Processing bulk upload...\n\nMetadata file: ' + uploadedMetadataFile + '\nZIP file: ' + uploadedZipFile);
    
    // Here you would typically:
    // 1. Parse the Excel metadata file
    // 2. Extract files from ZIP
    // 3. Match files with metadata
    // 4. Add to table
    
    $('#uploadModal').hide();
    resetUploadForm();
}

function resetUploadForm() {
    // Reset single upload form
    $('#fileName, #fileNumber').val('');
    $('#category, #subCategory, #department, #currentStatus').val('');
    $('#singleFileInput').val('');
    $('#revisionDate').val('');
    $('#revisionNo').val('R01');
    
    // Reset bulk upload form
    resetBulkUploadForm();
    
    // Reset uploaded file references
    uploadedMetadataFile = null;
    uploadedZipFile = null;
    
    // Reset to single upload tab
    $('#uploadModal .upload-tab').removeClass('active');
    $('#uploadModal .upload-tab[data-tab="single"]').addClass('active');
    $('.single-upload-options, .bulk-upload-options').removeClass('active');
    $('#singleUploadTab').addClass('active');
}

function addFileToTable(file, metadata = {}) {
    const fileExtension = file.name.split('.').pop().toLowerCase();
    const fileTypeClass = getFileTypeClass(fileExtension);
    const currentDate = getCurrentDateString();

    const fileName = metadata.fileName || file.name;
    const fileNumber = metadata.fileNumber || '';
    const revisionNo = metadata.revisionNo || 'R01';
    const status = metadata.status || 'Draft';
    const department = metadata.department || '';

    const newRow = `
        <tr>
            <td><span class="file-type-label ${fileTypeClass}">${fileExtension.toUpperCase()}</span></td>
            <td><input type="text" class="editable-input" value="${fileNumber}"></td>
            <td><input type="text" class="editable-input file-name-cell" value="${fileName}"></td>
            <td><input type="text" class="editable-input" value="${revisionNo}"></td>
            <td><input type="text" class="editable-input" value="${status}"></td>
            <td><input type="text" class="editable-input" value="${metadata.category || ''}"></td>
            <td><input type="text" class="editable-input" value=""></td>
            <td><input type="text" class="editable-input" value="${currentDate}"></td>
            <td><input type="text" class="editable-input" value="${currentDate}"></td>
            <td><input type="text" class="editable-input" value="${department}"></td>
            <td><input type="text" class="editable-input" value=""></td>
        </tr>
    `;

    $('.document-table tbody').append(newRow);
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

function showPreviewMetadata() {
    // Reset all fields to empty and highlight them
    $('#previewFileName, #previewFileNumber').val('');
    $('#previewCategory, #previewSubCategory, #previewDepartment, #previewCurrentStatus').val('');
    $('#previewRevisionDate').val('');

    // Add red highlighting to all required fields
    $('#previewFileName, #previewFileNumber, #previewRevisionDate, #previewCategory, #previewSubCategory, #previewDepartment, #previewCurrentStatus').addClass('empty-field');

    // Show error messages
    $('.error-message').show();

    // Show the modal
    $('#previewModal').css('display', 'flex');
}

function validatePreviewField(field) {
    const value = field.val().trim();
    const fieldId = field.attr('id');

    if (value === '' || value === null) {
        field.addClass('empty-field');
        field.siblings('.error-message').show();
    } else {
        field.removeClass('empty-field');
        field.siblings('.error-message').hide();
    }
}

function validatePreviewForm() {
    let isValid = true;
    const requiredFields = ['#previewFileName', '#previewFileNumber', '#previewRevisionDate',
        '#previewCategory', '#previewSubCategory', '#previewDepartment', '#previewCurrentStatus'];

    requiredFields.forEach(function (fieldId) {
        const field = $(fieldId);
        const value = field.val().trim();

        if (value === '' || value === null) {
            field.addClass('empty-field');
            field.siblings('.error-message').show();
            isValid = false;
        } else {
            field.removeClass('empty-field');
            field.siblings('.error-message').hide();
        }
    });

    return isValid;
}

function showUploadedFile(fileType, fileName) {
    if (fileType === 'metadata') {
        $('#metadataFileName').text(fileName);
        $('#metadataStatus').show();
        uploadedMetadataFile = fileName;
    } else if (fileType === 'zip') {
        $('#zipFileName').text(fileName);
        $('#zipStatus').show();
        uploadedZipFile = fileName;
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

function resetBulkUploadForm() {
    // Hide all uploaded file displays
    $('#metadataStatus, #zipStatus').hide();
    $('#metadataFileName, #zipFileName').text('');
    
    // Reset file references
    uploadedMetadataFile = null;
    uploadedZipFile = null;
}

// SEND DOCUMENTS FUNCTIONALITY

function showSendDocumentsModal() {
    // Clear form
    resetSendForm();
    
    // Populate attachments list with selected document
    if (selectedDocument) {
        const attachmentItem = `
            <div class="attachment-item">
                <span>${selectedDocument.fileName} (${selectedDocument.fileType})</span>
                <button type="button" onclick="removeAttachment(this)" style="background: #e53e3e; color: white; border: none; border-radius: 50%; width: 20px; height: 20px; cursor: pointer;">×</button>
            </div>
        `;
        $('#attachmentsList').html(attachmentItem);
    }
    
    // Show modal
    $('#sendDocumentsModal').css('display', 'flex');
}

function removeAttachment(button) {
    $(button).parent().remove();
}

function resetSendForm() {
    $('#sendTo, #sendCc, #sendSubject, #sendReason').val('');
    $('#responseExpected').val('');
    $('#targetResponseDate').val('');
    $('#attachmentsList').empty();
    
    // Remove character counters
    $('.char-counter').remove();
}

function validateSendForm() {
    const to = $('#sendTo').val().trim();
    const subject = $('#sendSubject').val().trim();
    const reason = $('#sendReason').val().trim();
    
    const errors = [];
    if (!to) errors.push('To field is required');
    if (to && !isValidEmail(to)) errors.push('Please enter a valid email address in To field');
    if (!subject) errors.push('Subject is required');
    if (!reason) errors.push('Reason for sending is required');
    
    const cc = $('#sendCc').val().trim();
    if (cc && !isValidEmail(cc)) errors.push('Please enter a valid email address in CC field');
    
    if (errors.length > 0) {
        alert('Please fix the following errors:\n\n' + errors.join('\n'));
        return false;
    }
    
    return true;
}

// UPDATE DOCUMENTS FUNCTIONALITY

function showUpdateDocumentsModal() {
    // Clear form and reset to single update tab
    resetUpdateForm();
    
    // Set single update tab as active
    $('#updateDocumentsModal .upload-tab').removeClass('active');
    $('#updateDocumentsModal .upload-tab[data-tab="single"]').addClass('active');
    $('.single-update-options, .bulk-update-options').removeClass('active');
    $('#singleUpdateTab').addClass('active');
    
    // Populate form with current document data
    if (selectedDocument) {
        $('#updateFileName').val(selectedDocument.fileName);
        $('#updateFileNumber').val(selectedDocument.fileNumber);
        $('#updateRevisionNo').val(selectedDocument.revisionNo);
        
        // Set revision date if available
        if (selectedDocument.revisionDate) {
            const dateParts = selectedDocument.revisionDate.split('.');
            if (dateParts.length === 3) {
                const formattedDate = `${dateParts[2]}-${dateParts[1].padStart(2, '0')}-${dateParts[0].padStart(2, '0')}`;
                $('#updateRevisionDate').val(formattedDate);
            }
        }
        
        // Set dropdown values
        $('#updateCurrentStatus').val(selectedDocument.status);
        $('#updateDepartment').val(selectedDocument.department);
        
        // Show current document info
        const currentDocumentItem = `
            <div class="current-document-item">
                <div class="document-info">
                    <div class="document-name">${selectedDocument.fileName}</div>
                    <div class="document-details">
                        File Number: ${selectedDocument.fileNumber} | 
                        Revision: ${selectedDocument.revisionNo} | 
                        Status: ${selectedDocument.status} | 
                        Department: ${selectedDocument.department}
                    </div>
                </div>
                <span class="file-type-badge">${selectedDocument.fileType}</span>
            </div>
        `;
        $('#currentDocumentInfo').html(currentDocumentItem);
    }
    
    // Show modal
    $('#updateDocumentsModal').css('display', 'flex');
}

function resetUpdateForm() {
    // Reset single update form
    $('#updateFileName, #updateFileNumber, #updateRevisionNo, #updateReason').val('');
    $('#updateRevisionDate').val('');
    $('#updateCategory, #updateSubCategory, #updateDepartment, #updateCurrentStatus').val('');
    $('#updateDocumentFile').val('');
    $('#currentDocumentInfo').empty();
    
    // Reset bulk update form
    resetBulkUpdateForm();
    
    // Reset uploaded file references
    updateUploadedMetadataFile = null;
    updateUploadedZipFile = null;
    
    // Remove character counters
    $('.char-counter').remove();
    
    // Reset to single update tab
    $('#updateDocumentsModal .upload-tab').removeClass('active');
    $('#updateDocumentsModal .upload-tab[data-tab="single"]').addClass('active');
    $('.single-update-options, .bulk-update-options').removeClass('active');
    $('#singleUpdateTab').addClass('active');
}

function validateUpdateForm() {
    const fileName = $('#updateFileName').val().trim();
    const fileNumber = $('#updateFileNumber').val().trim();
    const revisionDate = $('#updateRevisionDate').val();
    const category = $('#updateCategory').val();
    const subCategory = $('#updateSubCategory').val();
    const department = $('#updateDepartment').val();
    const status = $('#updateCurrentStatus').val();
    
    const errors = [];
    if (!fileName) errors.push('File Name is required');
    if (!fileNumber) errors.push('File Number is required');
    if (!revisionDate) errors.push('Revision Date is required');
    if (!category) errors.push('Category is required');
    if (!subCategory) errors.push('Sub-Category is required');
    if (!department) errors.push('Department is required');
    if (!status) errors.push('Current Status is required');
    
    if (errors.length > 0) {
        alert('Please fix the following errors:\n\n' + errors.join('\n'));
        return false;
    }
    
    return true;
}

function showUpdateUploadedFile(fileType, fileName) {
    if (fileType === 'update-metadata') {
        $('#updateMetadataFileName').text(fileName);
        $('#updateMetadataStatus').show();
        updateUploadedMetadataFile = fileName;
    } else if (fileType === 'update-zip') {
        $('#updateZipFileName').text(fileName);
        $('#updateZipStatus').show();
        updateUploadedZipFile = fileName;
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

function resetBulkUpdateForm() {
    // Hide all uploaded file displays for update
    $('#updateMetadataStatus, #updateZipStatus').hide();
    $('#updateMetadataFileName, #updateZipFileName').text('');
    
    // Reset file references
    updateUploadedMetadataFile = null;
    updateUploadedZipFile = null;
}

// Additional utility functions
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function formatDateForInput(dateString) {
    // Convert DD.MM.YYYY to YYYY-MM-DD format
    if (dateString && dateString.includes('.')) {
        const parts = dateString.split('.');
        if (parts.length === 3) {
            return `${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}`;
        }
    }
    return dateString;
}

function formatDateForDisplay(dateString) {
    // Convert YYYY-MM-DD to DD.MM.YYYY format
    if (dateString && dateString.includes('-')) {
        const parts = dateString.split('-');
        if (parts.length === 3) {
            return `${parts[2].padStart(2, '0')}.${parts[1].padStart(2, '0')}.${parts[0]}`;
        }
    }
    return dateString;
}

function generateNextRevision(currentRevision) {
    // Generate next revision number (R01 -> R02, etc.)
    if (currentRevision && currentRevision.startsWith('R')) {
        const num = parseInt(currentRevision.substring(1));
        if (!isNaN(num)) {
            return 'R' + String(num + 1).padStart(2, '0');
        }
    }
    return 'R01';
}

function showConfirmDialog(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

function showNotification(message, type = 'info') {
    // Simple notification system - you can enhance this with better UI
    const notification = $(`
        <div class="notification ${type}" style="
            position: fixed; 
            top: 20px; 
            right: 20px; 
            padding: 10px 20px; 
            background: ${type === 'success' ? '#4caf50' : type === 'error' ? '#f44336' : '#2196f3'}; 
            color: white; 
            border-radius: 4px; 
            z-index: 10000;
            max-width: 300px;
        ">
            ${message}
        </div>
    `);
    
    $('body').append(notification);
    
    setTimeout(() => {
        notification.fadeOut(() => {
            notification.remove();
        });
    }, 3000);
}

// Enhanced file type detection
function getFileTypeFromMime(file) {
    const mimeType = file.type.toLowerCase();
    
    if (mimeType.includes('pdf')) return 'pdf';
    if (mimeType.includes('word') || mimeType.includes('document')) return 'docx';
    if (mimeType.includes('sheet') || mimeType.includes('excel')) return 'xlsx';
    if (mimeType.includes('image/jpeg')) return 'jpeg';
    if (mimeType.includes('image/png')) return 'png';
    if (mimeType.includes('dwg')) return 'dwg';
    
    // Fallback to extension-based detection
    const extension = file.name.split('.').pop().toLowerCase();
    return extension;
}

// File size formatting
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Export functions for external use
window.DocumentManager = {
    showSendDocumentsModal,
    showUpdateDocumentsModal,
    resetUploadForm,
    resetSendForm,
    resetUpdateForm,
    validateSendForm,
    validateUpdateForm,
    showNotification
};


document.querySelectorAll('.sidebar-item').forEach(item => {
    item.addEventListener('click', function () {
        const targetPage = this.getAttribute('data-target');
        if (targetPage) {
            window.location.href = targetPage;
        }
    });
});


document.querySelectorAll('.sidebar-header[data-target]').forEach(item => {
    item.addEventListener('click', function () {
        const target = this.getAttribute('data-target');
        if (target) {
            window.location.href = target;
        }
    });
});


//Search and Filter functionality

$(document).ready(function () {
    // Function to highlight search matches in plain text cells
    function highlightText(text, term) {
        if(!term) return text;
        var reg = new RegExp('('+term.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&')+')', 'gi');
        return text.replace(reg, '<span class="highlight">$1</span>');
    }

    // Filtering logic + highlight
    $('.column-filter').on('input', function(){
        var $inputs = $('.filter-row .column-filter');
        var filterVals = [];
        $inputs.each(function(i, input){
            filterVals.push($(input).val().trim());
        });

        $('.document-table tbody tr').each(function(){
            var $row = $(this);
            var show = true;

            $row.find('td').each(function(i, cell){
                var filter = filterVals[i];
                var $input = $(cell).find('input');
                if ($input.length) {
                    // Cell has input field (do not destroy input)
                    var val = $input.val() || "";
                    $input.removeClass('highlight');
                    if(filter.length > 0) {
                        if(val.toLowerCase().indexOf(filter.toLowerCase()) === -1) {
                            show = false;
                        }
                    }
                } else {
                    // Plain text cell
                    var cellText = $(cell).text();
                    // Remove highlight
                    $(cell).html($(cell).text());
                    if(filter.length > 0) {
                        if(cellText.toLowerCase().indexOf(filter.toLowerCase()) === -1) {
                            show = false;
                        }
                    }
                }
            });

            $row.toggle(show);

            // Highlight only if showing
            if(show){
                $row.find('td').each(function(i, cell){
                    var filter = filterVals[i];
                    var $input = $(cell).find('input');
                    if ($input.length) {
                        // Input highlight with background color
                        var val = $input.val() || "";
                        if(filter.length > 0 && val.toLowerCase().indexOf(filter.toLowerCase()) !== -1) {
                            $input.addClass('highlight');
                        } else {
                            $input.removeClass('highlight');
                        }
                    } else {
                        if(filter.length > 0){
                            // Highlight plain text
                            var cellText = $(cell).text();
                            $(cell).html(highlightText(cellText, filter));
                        }
                    }
                });
            }
        });
    });
});


// Initialize DataTable for document table

$(document).ready(function() {
    $('#mainTable').DataTable({
        // Optional: Customize language to match your image more closely!
        "language": {
            "lengthMenu": "Show _MENU_ entries",
            "info": "Showing _START_ to _END_ of _TOTAL_ entries"
        },
        // Optional: Initial page length
        "pageLength": 10
    });
});



//multi-checkbox functionality

$(document).ready(function () {
    // Global variables
    let mainTableInstance = null;
    let columnFilters = {}; // Store selected filters for each column

    // Initialize DataTable
    function initializeDataTables() {
        if (!mainTableInstance && $('#mainTable').length) {
            mainTableInstance = $('#mainTable').DataTable({
                "language": {
                    "lengthMenu": "Show _MENU_ entries",
                    "info": "Showing _START_ to _END_ of _TOTAL_ entries"
                },
                "pageLength": 10,
                "destroy": true,
                "drawCallback": function () {
                    // Update filter dropdowns after table redraw
                    updateAllColumnFilters();
                }
            });
        }
    }

    // Enhanced Column Filter System
    function getColumnData(columnIndex) {
        const uniqueValues = new Set();

        $('#mainTable tbody tr:visible').each(function () {
            const cell = $(this).find('td').eq(columnIndex);
            let value = '';

            // Check if cell contains an input field
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

        // Add search box
        dropdown.append(`
        <input type="text" class="filter-search" placeholder="Search options...">
        <div class="filter-options"></div>
    `);

        // Get unique values from the column
        const uniqueValues = new Set();
        $('#mainTable tbody tr').each(function () {
            const cell = $(this).find('td').eq(columnIndex);
            let value = cell.find('input').length ? cell.find('input').val() : cell.text();
            value = value.trim();
            if (value) uniqueValues.add(value);
        });

        // Add checkboxes (all unchecked by default)
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
    $(document).on('click', '.filter-dropdown-toggle', function (e) {
        e.stopPropagation();
        const container = $(this).closest('.filter-container');
        const dropdown = container.find('.filter-dropdown');
        $('.filter-dropdown').not(dropdown).removeClass('show');
        dropdown.toggleClass('show');
        // Create dropdown content each time it's opened
        const columnIndex = $(this).siblings('.column-filter').data('column');
        createFilterDropdown(columnIndex);
    });

    // Hide dropdown when clicking outside
    $(document).on('click', function () {
        $('.filter-dropdown').removeClass('show');
    });

    // Prevent dropdown from closing when clicking inside the dropdown (including search box)
    $(document).on('click', '.filter-dropdown', function(e) {
        e.stopPropagation();
    });

    // Handle checkbox change
    $(document).on('change', '.filter-checkbox', function () {
        const columnIndex = $(this).data('column');
        columnFilters[columnIndex] = [];
        $(`.filter-dropdown[data-column="${columnIndex}"] .filter-checkbox:checked`).each(function () {
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
        $('#mainTable tbody tr').each(function () {
            let show = true;
            $(this).find('td').each(function (colIdx) {
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

    // Prevent typing in filter input
    $(document).on('keydown', '.column-filter', function (e) {
        e.preventDefault();
    });

    // Filter dropdown options as user types in the search box
    $(document).on('input', '.filter-search', function () {
        const searchTerm = $(this).val().toLowerCase();
        const options = $(this).siblings('.filter-options').find('.filter-option');
        options.each(function () {
            const label = $(this).find('label').text().toLowerCase();
            $(this).toggle(label.includes(searchTerm));
        });
    });

    // Right-click or context menu logic should store the row
    $('#mainTable').on('contextmenu', 'tr', function (e) {
        e.preventDefault();
        currentUpdateRow = $(this);
        // Show your context menu here...
        $('#contextMenu').css({ top: e.pageY, left: e.pageX, display: 'block' });
        $('#contextMenu').data('row', currentUpdateRow);
    });

    // When clicking "Update" in the context menu
    $(document).on('click', '.context-menu-item[data-action="update"]', function () {
        const $row = $('#contextMenu').data('row');
        if (!$row) return;

        // Autofill form fields from table row (adjust .eq() as per your table structure)
        $('#updateFileName').val($row.find('td').eq(2).find('input').val());
        $('#updateFileNumber').val($row.find('td').eq(1).find('input').val());
        $('#updateRevisionNo').val($row.find('td').eq(3).find('input').val());
        $('#updateRevisionDate').val($row.find('td').eq(8).find('input').val());
        $('#updateCategory').val($row.find('td').eq(5).find('input').val());
        $('#updateSubCategory').val(""); // Set if you have this in your table
        $('#updateDepartment').val($row.find('td').eq(9).find('input').val());
        $('#updateCurrentStatus').val($row.find('td').eq(4).find('input').val());
        $('#updateReason').val(""); // Set if you have this in your table

        $('#updateDocumentsModal').addClass('show');
        $('#contextMenu').hide();
    });

    // Hide Update Documents Modal when Cancel is clicked
    $('#cancelUpdate').on('click', function () {
        $('#updateDocumentsModal').removeClass('show');
        currentUpdateRow = null;
    });

    // Optional: Hide modal when clicking outside modal-content
    $('#updateDocumentsModal').on('click', function (e) {
        if ($(e.target).is('#updateDocumentsModal')) {
            $('#updateDocumentsModal').removeClass('show');
            currentUpdateRow = null;
        }
    });
});
