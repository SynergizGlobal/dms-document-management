var fullPath = window.location.pathname; 
var contextPath = "/" + fullPath.split("/")[1]; 
$(document).ajaxError(function(event, xhr, settings, thrownError) {
    if (xhr.status === 401) {
        window.location.href = contextPath + '/error.html';
    }
});

$(document).ready(function() {

	$.ajax({
				url: `${contextPath}/api/users/getsession`,
				method: 'GET',
				async: false, // token as query param
				success: function(response) {

					if(!response) {
						window.location.href = `${contextPath}/error.html`;
					}
					// You can proceed with further logic here
				},
				error: function(xhr, status, error) {
					console.error("Failed to set session:", error);
				}
			});
	$.ajax({
		url: `${contextPath}/api/users/get/username`,
		method: 'GET',
		async: false, // token as query param
		success: function(response) {
			$("#userName").text(response);
			console.log("Session set successfully:", response);
			// You can proceed with further logic here
		},
		error: function(xhr, status, error) {
			console.error("Failed to set session:", error);
		}
	});
});
$.ajax({
		url: `${contextPath}/api/users/get/userRole`,
		method: 'GET',
		async: false,
		success: function(response) {
			//$("#userName").text(response);
			if (response === 'Super user') {
				$("#navFilterForm").hide();
				
			}
			console.log("Session set successfully:", response);
			// You can proceed with further logic here
		},
		error: function(xhr, status, error) {
			console.error("Failed to set session:", error);
		}
	});
	
	// Toggle dropdown on username click
	   $("#userName").on("click", function(e) {
	       e.stopPropagation();
	       $("#userDropdown").toggle();
	   });

	   // Close dropdown when clicking anywhere else
	   $(document).on("click", function() {
	       $("#userDropdown").hide();
	   });

	   // Logout action
	   $("#logoutBtn").on("click", function() {
	       $.ajax({
	           url: `${contextPath}/api/users/logout`,
	           method: 'GET',
	           async: false,
	           success: function() {
	               window.location.href = "http://115.124.125.227/pmis/login";
	           },
	           error: function(__xhr__, __status__, error) {
	               console.error("Logout failed:", error);
	           }
	       });
	   });
let currentView = 'folders';
let searchVisible = false;
let navigationStack = [];

function loadArchivedFiles(folderid, foldername) {
	$("#breadcrumb-current").text($("#breadcrumb-current").text() + ' >' + foldername);
	fetch(`${contextPath}/api/documents/archived/folder-grid/${encodeURIComponent(folderid)}`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			projects: selectedProjects,   // send array
			contracts: selectedContracts  // send array
		})
	})
		.then(response => response.json())
		.then(folders => {
			const grid = document.querySelector(".folders-grid");
			grid.innerHTML = ""; // clear existing folders

			if (!folders || folders.length === 0) {
				grid.innerHTML = `<p style="text-align:center; color:gray;">No files available</p>`;
				return;
			}

			folders.forEach(folder => {
				const fileType = folder.fileType ? folder.fileType.toLowerCase() : "";

				// Pick icon based on file type
				let icon = `<i class="fa-solid fa-file" style="font-size:40px; color:#555;"></i>`;
				if (fileType === "pdf")
					icon = `<i class="fa-solid fa-file-pdf" style="font-size:40px; color:red;"></i>`;
				else if (["doc", "docx"].includes(fileType))
					icon = `<i class="fa-solid fa-file-word" style="font-size:40px; color:#2a5699;"></i>`;
				else if (["xls", "xlsx"].includes(fileType))
					icon = `<i class="fa-solid fa-file-excel" style="font-size:40px; color:#217346;"></i>`;
				else if (["png", "jpg", "jpeg", "gif"].includes(fileType))
					icon = `<i class="fa-solid fa-file-image" style="font-size:40px; color:#e3b341;"></i>`;
				else if (["ppt", "pptx"].includes(fileType))
					icon = `<i class="fa-solid fa-file-powerpoint" style="font-size:40px; color:#d24726;"></i>`;
				else if (["zip", "rar"].includes(fileType))
					icon = `<i class="fa-solid fa-file-zipper" style="font-size:40px; color:#f0a500;"></i>`;
				else if (["txt"].includes(fileType))
					icon = `<i class="fa-solid fa-file-lines" style="font-size:40px; color:#444;"></i>`;

				const folderCard = document.createElement("div");
				folderCard.className = "folder-card";
				folderCard.onclick = () => openFolder(folder.filePath, "file");

				folderCard.dataset.filePath = folder.filePath;
				folderCard.dataset.fileName = folder.fileName;
				folderCard.dataset.fileType = folder.fileType;
				folderCard.dataset.revisionNo = folder.revisionNo || '';
				folderCard.addEventListener("contextmenu", function(e) {
				    showContextMenu(e, this);
				});
				
				folderCard.innerHTML = `
                    <div class="file-icon">${icon}</div>
                    <div class="folder-title">${folder.fileName} ${folder.revisionNo} (${folder.fileType})</div>
                `;

				grid.appendChild(folderCard);

			});
			//loadArchiveFolder(folderid);
		})
		.catch(err => console.error("Error loading folders:", err));
}
function loadArchiveFolder(folderId) {
	const grid = document.querySelector(".folders-grid");
	//grid.innerHTML = ""; // clear existing folders
	folders = ["Archived"];

	folders.forEach(folder => {
		const folderCard = document.createElement("div");
		folderCard.className = "folder-card";
		folderCard.onclick = () => openFolder(folderId, "archive");

		folderCard.innerHTML = `
		                    <div class="folder-icon">
		                        <div class="folder-base">
		                            <div class="folder-tab"></div>
		                            <div class="folder-papers">
		                                <div class="paper paper-1"></div>
		                                <div class="paper paper-2"></div>
		                                <div class="paper paper-3"></div>
		                            </div>
		                            <div class="folder-label"></div>
		                        </div>
		                    </div>
		                    <div class="folder-title">${folder}</div>
		                `;

		grid.appendChild(folderCard);
	});
}
function loadSubFolders(folderid, foldername) {
	$("#breadcrumb-current").text('>' + foldername);
	fetch(`${contextPath}/api/subfolders/grid/${encodeURIComponent(folderid)}`
		, {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify({
				projects: selectedProjects,   // send array
				contracts: selectedContracts  // send array
			})
		})
		.then(response => response.json())
		.then(folders => {
			const grid = document.querySelector(".folders-grid");
			grid.innerHTML = ""; // clear existing folders

			if (!folders || folders.length === 0) {
				grid.innerHTML = `<p style="text-align:center; color:gray;">No folders available</p>`;
				return;
			}

			folders.forEach(folder => {
				const folderCard = document.createElement("div");
				folderCard.className = "folder-card";
				folderCard.onclick = () => openFolder(folder.id, "subfolder", folder.name);

				folderCard.innerHTML = `
	                    <div class="folder-icon">
	                        <div class="folder-base">
	                            <div class="folder-tab"></div>
	                            <div class="folder-papers">
	                                <div class="paper paper-1"></div>
	                                <div class="paper paper-2"></div>
	                                <div class="paper paper-3"></div>
	                            </div>
	                            <div class="folder-label"></div>
	                        </div>
	                    </div>
	                    <div class="folder-title">${folder.name}</div>
	                `;

				grid.appendChild(folderCard);
			});
		})
		.catch(err => console.error("Error loading folders:", err));
}
function loadCorrespondanceFiles(foldername) {
	$("#breadcrumb-current").text($("#breadcrumb-current").text() + ' >' + foldername);
	fetch(`${contextPath}/api/correspondence/getFolderFiles?type=${foldername}`, {
		method: "POST",
		body: JSON.stringify({
			projects: selectedProjects,   // send array
			contracts: selectedContracts  // send array
		}),
		headers: {
			"Content-Type": "application/json"
		}
	})
		.then(response => response.json())
		.then(folders => {
			const grid = document.querySelector(".folders-grid");
			grid.innerHTML = ""; // clear existing folders

			if (!folders || folders.length === 0) {
				grid.innerHTML = `<p style="text-align:center; color:gray;">No files available</p>`;
				return;
			}

			folders.forEach(folder => {
				const fileType = folder.fileType ? folder.fileType.toLowerCase() : "";

				// Pick icon based on file type
				let icon = `<i class="fa-solid fa-file" style="font-size:40px; color:#555;"></i>`;
				if (fileType === "pdf")
					icon = `<i class="fa-solid fa-file-pdf" style="font-size:40px; color:red;"></i>`;
				else if (["doc", "docx"].includes(fileType))
					icon = `<i class="fa-solid fa-file-word" style="font-size:40px; color:#2a5699;"></i>`;
				else if (["xls", "xlsx"].includes(fileType))
					icon = `<i class="fa-solid fa-file-excel" style="font-size:40px; color:#217346;"></i>`;
				else if (["png", "jpg", "jpeg", "gif"].includes(fileType))
					icon = `<i class="fa-solid fa-file-image" style="font-size:40px; color:#e3b341;"></i>`;
				else if (["ppt", "pptx"].includes(fileType))
					icon = `<i class="fa-solid fa-file-powerpoint" style="font-size:40px; color:#d24726;"></i>`;
				else if (["zip", "rar"].includes(fileType))
					icon = `<i class="fa-solid fa-file-zipper" style="font-size:40px; color:#f0a500;"></i>`;
				else if (["txt"].includes(fileType))
					icon = `<i class="fa-solid fa-file-lines" style="font-size:40px; color:#444;"></i>`;

				const folderCard = document.createElement("div");
				folderCard.className = "folder-card";
				folderCard.onclick = () => openFolder(folder.downloadUrl, "correspondencefile");
				folderCard.dataset.filePath = folder.filePath;
				folderCard.dataset.fileName = folder.fileName;
				folderCard.dataset.fileType = folder.fileType;
				folderCard.dataset.revisionNo = '';
				folderCard.dataset.downloadUrl = folder.downloadUrl;        // direct file URL
				folderCard.dataset.letterNumber = folder.letterNumber;
				folderCard.dataset.fromDept = folder.fromDept;
				folderCard.dataset.toDept = folder.toDept;
				folderCard.dataset.letterCode = folder.letterCode || '';
				folderCard.dataset.type = folder.type;
				folderCard.dataset.isCorrespondence = 'true';     
				folderCard.dataset.correspondenceId = folder.correspondenceId || '';       
				folderCard.addEventListener("contextmenu", function(e) {
				    showContextMenu(e, this);
				});
				folderCard.innerHTML = `
	                    <div class="file-icon">${icon}</div>
	                    <div class="folder-title">${folder.letterNumber}_${folder.fromDept}_${folder.toDept}_(${folder.fileType})</div>
	                `;

				grid.appendChild(folderCard);

			});
			loadArchiveFolder(folderid);
		})
		.catch(err => console.error("Error loading folders:", err));
}
function loadFiles(folderid, foldername) {
	$("#breadcrumb-current").text($("#breadcrumb-current").text() + ' >' + foldername);
	fetch(`${contextPath}/api/documents/folder-grid/${encodeURIComponent(folderid)}`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			projects: selectedProjects,   // send array
			contracts: selectedContracts  // send array
		})
	})
		.then(response => response.json())
		.then(folders => {
			const grid = document.querySelector(".folders-grid");
			grid.innerHTML = ""; // clear existing folders

			if (!folders || folders.length === 0) {
				grid.innerHTML = `<p style="text-align:center; color:gray;">No files available</p>`;
				return;
			}

			folders.forEach(folder => {
				const fileType = folder.fileType ? folder.fileType.toLowerCase() : "";

				// Pick icon based on file type
				let icon = `<i class="fa-solid fa-file" style="font-size:40px; color:#555;"></i>`;
				if (fileType === "pdf")
					icon = `<i class="fa-solid fa-file-pdf" style="font-size:40px; color:red;"></i>`;
				else if (["doc", "docx"].includes(fileType))
					icon = `<i class="fa-solid fa-file-word" style="font-size:40px; color:#2a5699;"></i>`;
				else if (["xls", "xlsx"].includes(fileType))
					icon = `<i class="fa-solid fa-file-excel" style="font-size:40px; color:#217346;"></i>`;
				else if (["png", "jpg", "jpeg", "gif"].includes(fileType))
					icon = `<i class="fa-solid fa-file-image" style="font-size:40px; color:#e3b341;"></i>`;
				else if (["ppt", "pptx"].includes(fileType))
					icon = `<i class="fa-solid fa-file-powerpoint" style="font-size:40px; color:#d24726;"></i>`;
				else if (["zip", "rar"].includes(fileType))
					icon = `<i class="fa-solid fa-file-zipper" style="font-size:40px; color:#f0a500;"></i>`;
				else if (["txt"].includes(fileType))
					icon = `<i class="fa-solid fa-file-lines" style="font-size:40px; color:#444;"></i>`;

				const folderCard = document.createElement("div");
				folderCard.className = "folder-card";
				folderCard.onclick = () => openFolder(folder.filePath, "file");

				folderCard.innerHTML = `
                    <div class="file-icon">${icon}</div>
                    <div class="folder-title">${folder.fileName} ${folder.revisionNo} (${folder.fileType})</div>
                `;

				grid.appendChild(folderCard);

			});
			loadArchiveFolder(folderid);
		})
		.catch(err => console.error("Error loading folders:", err));
}


// Sample document data for correspondence
const documentsData = {
	inward: {
		'pmc-employer': [
			{ id: 1, name: 'PMC Monthly Report - January 2024', type: 'PDF', date: '2024-01-15' },
			{ id: 2, name: 'Quality Inspection Report', type: 'DOCX', date: '2024-01-20' },
			{ id: 3, name: 'Safety Compliance Certificate', type: 'PDF', date: '2024-01-25' }
		],
		'subcontractor': [
			{ id: 4, name: 'Subcontractor Agreement Amendment', type: 'PDF', date: '2024-01-10' },
			{ id: 5, name: 'Progress Report - Week 3', type: 'XLSX', date: '2024-01-18' }
		],
		'interface-agency': [
			{ id: 6, name: 'Interface Coordination Meeting Minutes', type: 'DOCX', date: '2024-01-12' },
			{ id: 7, name: 'Technical Specification Update', type: 'PDF', date: '2024-01-22' }
		],
		'state-govt': [
			{ id: 8, name: 'State Government Clearance Letter', type: 'PDF', date: '2024-01-08' },
			{ id: 9, name: 'Environmental Compliance Report', type: 'DOCX', date: '2024-01-19' }
		],
		'ir': [
			{ id: 10, name: 'Indian Railways Approval Letter', type: 'PDF', date: '2024-01-11' },
			{ id: 11, name: 'Track Safety Guidelines', type: 'PDF', date: '2024-01-26' }
		],
		'dab': [
			{ id: 12, name: 'DAB Decision Record', type: 'PDF', date: '2024-01-13' },
			{ id: 13, name: 'Dispute Resolution Minutes', type: 'DOCX', date: '2024-01-27' }
		]
	},
	outward: {
		'pmc-employer': [
			{ id: 14, name: 'Request for Arrangement of 60 Kg 880 Grade Prime Rails', type: 'PDF', date: '2024-01-05' },
			{ id: 15, name: 'Monthly Progress Summary', type: 'DOCX', date: '2024-01-30' }
		],
		'subcontractor': [
			{ id: 16, name: 'Work Order Modification', type: 'PDF', date: '2024-01-08' },
			{ id: 17, name: 'Payment Authorization', type: 'XLSX', date: '2024-01-28' }
		],
		'interface-agency': [
			{ id: 18, name: 'Coordination Request Letter', type: 'PDF', date: '2024-01-09' },
			{ id: 19, name: 'Technical Query Response', type: 'DOCX', date: '2024-01-23' }
		],
		'state-govt': [
			{ id: 20, name: 'Land Acquisition Request', type: 'PDF', date: '2024-01-07' },
			{ id: 21, name: 'Permission Application', type: 'DOCX', date: '2024-01-21' }
		],
		'ir': [
			{ id: 22, name: 'Track Safety Submission', type: 'PDF', date: '2024-01-10' },
			{ id: 23, name: 'Operational Clearance Request', type: 'PDF', date: '2024-01-24' }
		],
		'internal': [
			{ id: 24, name: 'Internal Memo - Project Status', type: 'DOCX', date: '2024-01-12' },
			{ id: 25, name: 'Team Meeting Minutes', type: 'PDF', date: '2024-01-29' }
		],
		'dab': [
			{ id: 26, name: 'DAB Referral Letter', type: 'PDF', date: '2024-01-15' },
			{ id: 27, name: 'Evidence Submission', type: 'PDF', date: '2024-01-31' }
		],
		'ircon-co': [
			{ id: 28, name: 'Shifting of old CSM stabled in Ancheli Yard', type: 'PDF', date: '2024-01-14' },
			{ id: 29, name: 'Corporate Communication', type: 'DOCX', date: '2024-01-17' }
		],
		'dfccil': [
			{ id: 30, name: 'Notice of claim under Clause 13.7 & 20.1 of GCC/PC due to change in GST rate', type: 'PDF', date: '2024-01-16' },
			{ id: 31, name: 'DFCCIL Coordination Letter', type: 'PDF', date: '2024-01-25' }
		]
	}
};

function openFolder(folderid, type, foldername) {
	// Add click animation
	event.currentTarget.style.transform = 'scale(0.95)';
	setTimeout(() => {
		event.currentTarget.style.transform = 'translateY(-5px)';
	}, 150);
	if (type == "correspondence")
		loadCorrespondenceInboundAndOutbound("Correspondence");
	if (type == "folder")
		loadSubFolders(folderid, foldername);
	if (type == "subfolder") {
		loadFiles(folderid, foldername);
		//loadArchiveFolder(folderid);
	}
	if (type == "file")
		window.open(`${contextPath}/api/documents/view?path=${encodeURIComponent(folderid)}`, "_blank");
	if (type == "archive")
		loadArchivedFiles(folderid, "Archived");
	if (type == "Incoming")
		loadCorrespondanceFiles("Incoming");
	if (type == "Outgoing")
		loadCorrespondanceFiles("Outgoing");
	if (type == "correspondencefile")
		window.open(folderid, "_blank");
	/*if (folderName === 'drawings') {
		showDrawingsView();
	} else if (folderName === 'correspondence') {
		showCorrespondenceView();
	} else if (folderName === 'moms') {
		showMomsView();
	} else {
		// For other folders, show alert
		alert(`Opening ${folderName} folder...`);
		console.log(`User clicked on ${folderName} folder`);
	}*/
}

function openDrawingFolder(folderName) {
	// Add click animation
	event.currentTarget.style.transform = 'scale(0.95)';
	setTimeout(() => {
		event.currentTarget.style.transform = 'translateY(-5px)';
	}, 150);

	alert(`Opening drawing folder: ${folderName}`);
	console.log(`User clicked on drawing folder: ${folderName}`);
}

function openCorrespondenceFolder(folderName) {
	// Add click animation
	event.currentTarget.style.transform = 'scale(0.95)';
	setTimeout(() => {
		event.currentTarget.style.transform = 'translateY(-5px)';
	}, 150);

	if (folderName === 'inward') {
		showInwardView();
	} else if (folderName === 'outward') {
		showOutwardView();
	} else {
		alert(`Opening correspondence folder: ${folderName}`);
		console.log(`User clicked on correspondence folder: ${folderName}`);
	}
}

function openInwardFolder(folderName) {
	// Add click animation
	event.currentTarget.style.transform = 'scale(0.95)';
	setTimeout(() => {
		event.currentTarget.style.transform = 'translateY(-5px)';
	}, 150);

	showDocuments('inward', folderName);
}

function openOutwardFolder(folderName) {
	// Add click animation
	event.currentTarget.style.transform = 'scale(0.95)';
	setTimeout(() => {
		event.currentTarget.style.transform = 'translateY(-5px)';
	}, 150);

	showDocuments('outward', folderName);
}

function openMomsFolder(folderName) {
	// Add click animation
	event.currentTarget.style.transform = 'scale(0.95)';
	setTimeout(() => {
		event.currentTarget.style.transform = 'translateY(-5px)';
	}, 150);

	alert(`Opening moms folder: ${folderName}`);
	console.log(`User clicked on moms folder: ${folderName}`);
}

// Helper function to hide all views except the main folders view
function hideAllViews() {
	document.getElementById('drawingsView').style.display = 'none';
	document.getElementById('correspondenceView').style.display = 'none';
	document.getElementById('inwardView').style.display = 'none';
	document.getElementById('outwardView').style.display = 'none';
	document.getElementById('documentsView').style.display = 'none';
	document.getElementById('momsView').style.display = 'none';

	// Check if reportsView exists and hide it
	const reportsView = document.getElementById('reportsView');
	if (reportsView) {
		reportsView.style.display = 'none';
	}
}

function showDrawingsView() {
	currentView = 'drawings';

	// Hide all other views
	document.getElementById('foldersView').style.display = 'none';
	hideAllViews();

	// Show drawings view
	document.getElementById('drawingsView').style.display = 'block';

	// Update breadcrumb
	document.getElementById('breadcrumb-separator').style.display = 'inline';
	document.getElementById('breadcrumb-current').textContent = '1 Drawings';

	// Update search placeholder
	const searchInput = document.getElementById('searchInput');
	searchInput.placeholder = 'Search drawing folders...';
}

function showCorrespondenceView() {
	currentView = 'correspondence';

	// Hide all other views
	document.getElementById('foldersView').style.display = 'none';
	hideAllViews();

	// Show correspondence view
	document.getElementById('correspondenceView').style.display = 'block';

	// Update breadcrumb
	document.getElementById('breadcrumb-separator').style.display = 'inline';
	document.getElementById('breadcrumb-current').textContent = '2 Correspondence';

	// Update search placeholder
	const searchInput = document.getElementById('searchInput');
	searchInput.placeholder = 'Search correspondence folders...';
}

function showInwardView() {
	currentView = 'inward';

	// Hide all other views except inward
	hideAllViews();

	// Show inward view
	document.getElementById('inwardView').style.display = 'block';

	// Update breadcrumb
	document.getElementById('breadcrumb-current').textContent = '2 Correspondence > 201 Inward';

	// Update search placeholder
	const searchInput = document.getElementById('searchInput');
	searchInput.placeholder = 'Search inward folders...';
}

function showOutwardView() {
	currentView = 'outward';

	// Hide all other views except outward
	hideAllViews();

	// Show outward view
	document.getElementById('outwardView').style.display = 'block';

	// Update breadcrumb
	document.getElementById('breadcrumb-current').textContent = '2 Correspondence > 202 Outward';

	// Update search placeholder
	const searchInput = document.getElementById('searchInput');
	searchInput.placeholder = 'Search outward folders...';
}

function showDocuments(type, category) {
	currentView = 'documents';

	const documents = documentsData[type][category] || [];
	const documentsList = document.getElementById('documentsList');

	// Hide all other views except documents
	hideAllViews();

	// Show documents view
	document.getElementById('documentsView').style.display = 'block';

	documentsList.innerHTML = documents.map(doc => `
        <div class="document-item" onclick="openDocument(${doc.id})">
            <div class="document-icon">📄</div>
            <div class="document-info">
                <h4>${doc.name}</h4>
                <p>${doc.type} • ${doc.date}</p>
            </div>
        </div>
    `).join('');

	// Update breadcrumb
	const categoryTitle = category.replace('-', ' ').toUpperCase();
	const typeTitle = type === 'inward' ? '201 Inward' : '202 Outward';
	document.getElementById('breadcrumb-current').textContent = `2 Correspondence > ${typeTitle} > ${categoryTitle}`;

	// Update search placeholder
	const searchInput = document.getElementById('searchInput');
	searchInput.placeholder = 'Search documents...';
}

function showMomsView() {
	currentView = 'moms';

	// Hide all other views
	document.getElementById('foldersView').style.display = 'none';
	hideAllViews();

	// Show moms view
	document.getElementById('momsView').style.display = 'block';

	// Update breadcrumb
	document.getElementById('breadcrumb-separator').style.display = 'inline';
	document.getElementById('breadcrumb-current').textContent = '3 MOMs';

	// Update search placeholder
	const searchInput = document.getElementById('searchInput');
	searchInput.placeholder = 'Search moms folders...';
}

function hideCorrespondenceViews() {
	document.getElementById('correspondenceView').style.display = 'none';
	document.getElementById('inwardView').style.display = 'none';
	document.getElementById('outwardView').style.display = 'none';
	document.getElementById('documentsView').style.display = 'none';
}

function openDocument(docId) {
	alert(`Opening document ID: ${docId}`);
	console.log(`User clicked on document ID: ${docId}`);
}

function goHome() {
	currentView = 'folders';

	// Show main folders view
	document.getElementById('foldersView').style.display = 'block';

	// Hide ALL other views using the comprehensive hide function
	hideAllViews();

	// Update breadcrumb
	document.getElementById('breadcrumb-separator').style.display = 'none';
	document.getElementById('breadcrumb-current').textContent = '';

	// Update search placeholder
	const searchInput = document.getElementById('searchInput');
	searchInput.placeholder = 'Search folders...';
	searchInput.value = '';

	// Show all folders if search was active
	document.querySelectorAll('.folder-card').forEach(folder => {
		folder.style.display = 'flex';
	});

	loadFolders(selectedProjects, selectedContracts);
}

function toggleSearch() {
	const searchInput = document.getElementById('searchInput');
	searchVisible = !searchVisible;

	if (searchVisible) {
		searchInput.style.display = 'block';
		searchInput.focus();
	} else {
		searchInput.style.display = 'none';
		searchInput.value = '';

		// Show all items based on current view
		let targetSelector = '';
		switch (currentView) {
			case 'folders':
				targetSelector = '#foldersView .folder-card';
				break;
			case 'drawings':
				targetSelector = '#drawingsView .folder-card';
				break;
			case 'correspondence':
				targetSelector = '#correspondenceView .folder-card';
				break;
			case 'inward':
				targetSelector = '#inwardView .folder-card';
				break;
			case 'outward':
				targetSelector = '#outwardView .folder-card';
				break;
			case 'documents':
				targetSelector = '#documentsView .document-item';
				break;
			case 'moms':
				targetSelector = '#momsView .folder-card';
				break;
		}

		if (targetSelector) {
			document.querySelectorAll(targetSelector).forEach(item => {
				item.style.display = 'flex';
			});
		}
	}
}

// Add navigation functionality
document.querySelectorAll('.nav-item').forEach(item => {
	item.addEventListener('click', function() {
		// Remove active class from all nav items
		document.querySelectorAll('.nav-item').forEach(nav => nav.classList.remove('active'));
		// Add active class to clicked item
		this.classList.add('active');
	});
});

// Add search functionality
document.addEventListener('DOMContentLoaded', function() {
	const searchInput = document.getElementById('searchInput');

	searchInput.addEventListener('input', function(e) {
		const searchTerm = e.target.value.toLowerCase();
		let targetSelector = '';

		// Determine what to search based on current view
		switch (currentView) {
			case 'folders':
				targetSelector = '#foldersView .folder-card';
				break;
			case 'drawings':
				targetSelector = '#drawingsView .folder-card';
				break;
			case 'correspondence':
				targetSelector = '#correspondenceView .folder-card';
				break;
			case 'inward':
				targetSelector = '#inwardView .folder-card';
				break;
			case 'outward':
				targetSelector = '#outwardView .folder-card';
				break;
			case 'documents':
				targetSelector = '#documentsView .document-item';
				break;
			case 'moms':
				targetSelector = '#momsView .folder-card';
				break;
		}

		if (targetSelector.includes('.folder-card')) {
			const folders = document.querySelectorAll(targetSelector);
			folders.forEach(folder => {
				const title = folder.querySelector('.folder-title').textContent.toLowerCase();
				folder.style.display = title.includes(searchTerm) ? 'flex' : 'none';
			});
		} else if (targetSelector.includes('.document-item')) {
			const documents = document.querySelectorAll(targetSelector);
			documents.forEach(doc => {
				const title = doc.querySelector('h4').textContent.toLowerCase();
				doc.style.display = title.includes(searchTerm) ? 'flex' : 'none';
			});
		}
	});
});

// Add folder hover effects
document.addEventListener('DOMContentLoaded', function() {
	document.querySelectorAll('.folder-card').forEach(card => {
		card.addEventListener('mouseenter', function() {
			this.style.borderColor = '#2c5aa0';
		});

		card.addEventListener('mouseleave', function() {
			this.style.borderColor = '#e9ecef';
		});
	});
});

/////////////////
const $projectFilterInput = $(".project-column-filter");
const $contractFilterInput = $(".contract-column-filter");
const $projectDropdown = $("#projectFilter");
const $contractDropdown = $("#contractFilter");
const $projectToggle = $(".projectfilter-dropdown-toggle");
const $contractToggle = $(".contractfilter-dropdown-toggle");
// 🔹 Fetch data from backend API
const projectAPI = `${contextPath}/api/projects/get/for-folder-grid`
const contractAPI = `${contextPath}/api/contracts/get/for-folder-grid`

function createFilter(api, $dropdown, $toggle, $filterInput) {
	$.get(api, function(data) {
		// Add search input at top
		$dropdown.append('<input type="text" class="dropdown-search" placeholder="Search...">');

		// Populate checkbox list
		data.forEach(item => {
			$dropdown.append(
				`<label><input type="checkbox" value="${item}"> ${item}</label>`
			);
		});

		// 🔹 Search inside dropdown
		$dropdown.on("keyup", ".dropdown-search", function() {
			const term = $(this).val().toLowerCase();
			$dropdown.find("label").each(function() {
				$(this).toggle($(this).text().toLowerCase().includes(term));
			});
		});

		// 🔹 Handle selection
		$dropdown.on("change", "input[type=checkbox]", function() {
			let selected = [];
			$dropdown.find("input:checked").each(function() {
				selected.push($(this).val());
			});
			$filterInput.val(selected.join(", ")); // Show selected in input box
			$("#breadcrumb-current").text('');
		});
	});

	// 🔹 Toggle dropdown open/close
	$toggle.on("click", function() {
		$dropdown.toggle();
	});

	// 🔹 Hide dropdown when clicking outside
	$(document).on("click", function(e) {
		if (!$(e.target).closest(".filter-container").length) {
			$dropdown.hide();
		}
	});
}
createFilter(projectAPI, $projectDropdown, $projectToggle, $projectFilterInput);
createFilter(contractAPI, $contractDropdown, $contractToggle, $contractFilterInput);
let selectedProjects = [];
let selectedContracts = [];
$(document).on("change", ".filter-dropdown input[type='checkbox']", function() {
	let container = $(this).closest(".filter-container");
	let selected = [];

	container.find("input[type='checkbox']:checked").each(function() {
		selected.push($(this).val());
	});

	// Update input field with selected values
	if (selected.length > 0) {
		container.find("input[type='text']").val(selected.join(", "));
	} else {
		container.find("input[type='text']").val("");
	}

	// Save separately for project/contract
	if (container.find("#projectFilter").length > 0) {
		selectedProjects = selected;
	}
	if (container.find("#contractFilter").length > 0) {
		selectedContracts = selected;
	}
	// Usage
	//const projectStr = toSqlInClause(selectedProjects);
	//const contractStr = toSqlInClause(selectedContracts);
	loadFolders(selectedProjects, selectedContracts);

});

// Convert array into string with quotes
function toSqlInClause(array) {
	return array.map(item => `'${item}'`).join(",");
}

function loadFolders(projects, contracts) {
	fetch(`${contextPath}/api/folders/grid`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			projects: projects,   // send array
			contracts: contracts  // send array
		})
	})
		.then(response => {
			if (!response.ok) {
				throw new Error("Failed to load folders");
			}
			return response.json();
		})
		.then(folders => {
			const grid = document.querySelector(".folders-grid");
			grid.innerHTML = ""; // clear existing folders

			if (!folders || folders.length === 0) {
				//grid.innerHTML = `<p style="text-align:center; color:gray;">No folders available</p>`;
				loadCorrespondence();
				return;
			}

			folders.forEach(folder => {
				const folderCard = document.createElement("div");
				folderCard.className = "folder-card";
				folderCard.onclick = () => openFolder(folder.id, "folder", folder.name);

				folderCard.innerHTML = `
                <div class="folder-icon">
                    <div class="folder-base">
                        <div class="folder-tab"></div>
                        <div class="folder-papers">
                            <div class="paper paper-1"></div>
                            <div class="paper paper-2"></div>
                            <div class="paper paper-3"></div>
                        </div>
                        <div class="folder-label"></div>
                    </div>
                </div>
                <div class="folder-title">${folder.name}</div>
            `;

				grid.appendChild(folderCard);
			});
			loadCorrespondence();
		})
		.catch(err => console.error("Error loading folders:", err));
}
//loadCorrespondence();
function loadCorrespondence() {
	const grid = document.querySelector(".folders-grid");
	//grid.innerHTML = ""; // clear existing folders
	const folders = ["Correspondence"]
	if (!folders || folders.length === 0) {
		grid.innerHTML = `<p style="text-align:center; color:gray;">No folders available</p>`;
		return;
	}

	folders.forEach(folder => {
		const folderCard = document.createElement("div");
		folderCard.className = "folder-card";
		folderCard.onclick = () => openFolder(folder, "correspondence");

		folderCard.innerHTML = `
	                <div class="folder-icon">
	                    <div class="folder-base">
	                        <div class="folder-tab"></div>
	                        <div class="folder-papers">
	                            <div class="paper paper-1"></div>
	                            <div class="paper paper-2"></div>
	                            <div class="paper paper-3"></div>
	                        </div>
	                        <div class="folder-label"></div>
	                    </div>
	                </div>
	                <div class="folder-title">${folder}</div>
	            `;

		grid.appendChild(folderCard);
	});
}
function loadCorrespondenceInboundAndOutbound(foldername) {
	$("#breadcrumb-current").text($("#breadcrumb-current").text() + ' >' + foldername);
	const grid = document.querySelector(".folders-grid");
	grid.innerHTML = ""; // clear existing folders
	const folders = ["Incoming", "Outgoing"]
	if (!folders || folders.length === 0) {
		grid.innerHTML = `<p style="text-align:center; color:gray;">No folders available</p>`;
		return;
	}

	folders.forEach(folder => {
		const folderCard = document.createElement("div");
		folderCard.className = "folder-card";
		folderCard.onclick = () => openFolder(folder, folder);

		folderCard.innerHTML = `
		                <div class="folder-icon">
		                    <div class="folder-base">
		                        <div class="folder-tab"></div>
		                        <div class="folder-papers">
		                            <div class="paper paper-1"></div>
		                            <div class="paper paper-2"></div>
		                            <div class="paper paper-3"></div>
		                        </div>
		                        <div class="folder-label"></div>
		                    </div>
		                </div>
		                <div class="folder-title">${folder}</div>
		            `;

		grid.appendChild(folderCard);
	});
}
////
let currentFileData = {};

function showContextMenu(e, card) {
    e.preventDefault();
    e.stopPropagation();

    // Save file data
	currentFileData = {
		   correspondenceId: card.dataset.correspondenceId,
	       filePath: card.dataset.filePath,
	       fileName: card.dataset.fileName,
	       fileType: card.dataset.fileType,
	       revisionNo: card.dataset.revisionNo,
	       downloadUrl: card.dataset.downloadUrl,
	       letterNumber: card.dataset.letterNumber || '',
	       fromDept: card.dataset.fromDept || '',
	       toDept: card.dataset.toDept || '',
	       letterCode: card.dataset.letterCode || '',
	       type: card.dataset.type || '',
	       isCorrespondence: card.dataset.isCorrespondence === 'true'
	   };

    // Position and show menu
    const menu = document.getElementById("fileContextMenu");
    menu.style.display = "block";
    menu.style.left = e.clientX + "px";
    menu.style.top = e.clientY + "px";
}

// Hide menu on outside click
document.addEventListener("click", function() {
    document.getElementById("fileContextMenu").style.display = "none";
});

// VIEW
document.getElementById("ctxView").addEventListener("click", function() {
    if (currentFileData.isCorrespondence) {
        // Open file directly via downloadUrl
        window.open(currentFileData.downloadUrl, "_blank");
    } else {
        window.open(`${contextPath}/api/documents/view?path=${encodeURIComponent(currentFileData.filePath)}`, "_blank");
    }
    document.getElementById("fileContextMenu").style.display = "none";
});

// DOWNLOAD
document.getElementById("ctxDownload").addEventListener("click", function() {
    if (currentFileData.isCorrespondence) {
        // Download via downloadUrl
        const a = document.createElement("a");
        a.href = currentFileData.downloadUrl;
        a.download = currentFileData.fileName;
        a.click();
    } else {
        const a = document.createElement("a");
        a.href = `${contextPath}/api/documents/download?path=${encodeURIComponent(currentFileData.filePath)}`;
        a.download = currentFileData.fileName;
        a.click();
    }
    document.getElementById("fileContextMenu").style.display = "none";
});

// DETAILS
 document.getElementById("ctxDetails").addEventListener("click", function() {
    let detailsHtml = '';

    if (currentFileData.isCorrespondence) {
        detailsHtml = `
            <table style="width:100%; border-collapse:collapse; font-size:13px;">
                <tr style="border-bottom:1px solid #eee;">
                    <td style="padding:8px; color:#666; font-weight:600; width:40%;">File Name</td>
                    <td style="padding:8px; word-break:break-all; width:60%;">${currentFileData.fileName}</td>
                </tr>
                <tr style="border-bottom:1px solid #eee;">
                    <td style="padding:8px; color:#666; font-weight:600;">File Type</td>
                    <td style="padding:8px;">${currentFileData.fileType?.toUpperCase()}</td>
                </tr>
                <tr style="border-bottom:1px solid #eee;">
                    <td style="padding:8px; color:#666; font-weight:600;">Letter Number</td>
                    <td style="padding:8px;">
                     <a href="${contextPath}/view.html?id=${encodeURIComponent(currentFileData.correspondenceId)}"
                           target="_blank"
                           style="color:#2c5aa0; font-weight:600; text-decoration:underline; cursor:pointer;">
                            ${currentFileData.letterNumber || 'N/A'}
                        </a>
                    </td>
                </tr>
                <tr style="border-bottom:1px solid #eee;">
                    <td style="padding:8px; color:#666; font-weight:600;">From Dept</td>
                    <td style="padding:8px;">${currentFileData.fromDept || 'N/A'}</td>
                </tr>
                <tr style="border-bottom:1px solid #eee;">
                    <td style="padding:8px; color:#666; font-weight:600;">To Dept</td>
                    <td style="padding:8px;">${currentFileData.toDept || 'N/A'}</td>
                </tr>
                <tr style="border-bottom:1px solid #eee;">
                    <td style="padding:8px; color:#666; font-weight:600;">Type</td>
                    <td style="padding:8px;">${currentFileData.type || 'N/A'}</td>
                </tr>
                <tr>
                    <td style="padding:8px; color:#666; font-weight:600;">Letter Code</td>
                    <td style="padding:8px;">${currentFileData.letterCode || 'N/A'}</td>
                </tr>
            </table>
        `;
    } else {
        detailsHtml = `
            <table style="width:100%; border-collapse:collapse; font-size:13px;">
                <tr style="border-bottom:1px solid #eee;">
                    <td style="padding:8px; color:#666; font-weight:600; width:40%;">File Name</td>
                    <td style="padding:8px; word-break:break-all; width:60%;">${currentFileData.fileName}</td>
                </tr>
                <tr style="border-bottom:1px solid #eee;">
                    <td style="padding:8px; color:#666; font-weight:600;">File Type</td>
                    <td style="padding:8px;">${currentFileData.fileType?.toUpperCase()}</td>
                </tr>
                <tr>
                    <td style="padding:8px; color:#666; font-weight:600;">Revision No</td>
                    <td style="padding:8px;">${currentFileData.revisionNo || 'N/A'}</td>
                </tr>
            </table>
        `;
    }

    document.getElementById("detailsContent").innerHTML = detailsHtml;
    document.getElementById("detailsModal").style.display = "block";
    document.getElementById("fileContextMenu").style.display = "none";
});

//////////////////