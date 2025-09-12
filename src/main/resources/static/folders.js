let currentView = 'folders';
let searchVisible = false;
let navigationStack = [];

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

function openFolder(folderName) {
    // Add click animation
    event.currentTarget.style.transform = 'scale(0.95)';
    setTimeout(() => {
        event.currentTarget.style.transform = 'translateY(-5px)';
    }, 150);

    if (folderName === 'drawings') {
        showDrawingsView();
    } else if (folderName === 'correspondence') {
        showCorrespondenceView();
    } else if (folderName === 'moms') {
        showMomsView();
    } else {
        // For other folders, show alert
        alert(`Opening ${folderName} folder...`);
        console.log(`User clicked on ${folderName} folder`);
    }
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
        switch(currentView) {
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
    item.addEventListener('click', function () {
        // Remove active class from all nav items
        document.querySelectorAll('.nav-item').forEach(nav => nav.classList.remove('active'));
        // Add active class to clicked item
        this.classList.add('active');
    });
});

// Add search functionality
document.addEventListener('DOMContentLoaded', function () {
    const searchInput = document.getElementById('searchInput');

    searchInput.addEventListener('input', function (e) {
        const searchTerm = e.target.value.toLowerCase();
        let targetSelector = '';
        
        // Determine what to search based on current view
        switch(currentView) {
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
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.folder-card').forEach(card => {
        card.addEventListener('mouseenter', function () {
            this.style.borderColor = '#2c5aa0';
        });

        card.addEventListener('mouseleave', function () {
            this.style.borderColor = '#e9ecef';
        });
    });
});