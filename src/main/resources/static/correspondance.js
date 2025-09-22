// Base API configuration
const API_BASE_URL = 'http://localhost:8000/dms/api/correspondence';

// Email validation function
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Sidebar navigation
document.querySelectorAll('.sidebar-item').forEach(item => {
    item.addEventListener('click', function () {
        document.querySelectorAll('.sidebar-item').forEach(i => i.classList.remove('active'));
        this.classList.add('active');
    });
});

// Storage for uploaded letters data
let lettersData = {};
let letterCounter = 1;
let currentAttachMode = false;
let attachToLetterNo = null;

// Upload Modal Elements
const uploadBtn = document.getElementById('uploadBtn');
const uploadModal = document.getElementById('uploadModal');
const closeModal = document.getElementById('closeModal');
const cancelBtn = document.getElementById('cancelBtn');
const sendBtn = document.getElementById('sendBtn');
const saveAsDraftBtn = document.getElementById('saveAsDraftBtn');
const addAttachmentBtn = document.getElementById('addAttachmentBtn');
const attachmentInput = document.getElementById('attachmentInput');
const attachmentsList = document.getElementById('attachmentsList');

// Letter Details Modal Elements
const letterDetailsModal = document.getElementById('letterDetailsModal');
const closeLetterModal = document.getElementById('closeLetterModal');
const attachBtn = document.getElementById('attachBtn');
const replyBtn = document.getElementById('replyBtn');
const cancelDetailBtn = document.getElementById('cancelDetailBtn');

// Open upload modal
uploadBtn.addEventListener('click', function () {
    currentAttachMode = false;
    attachToLetterNo = null;
    updateModalForMode();
    uploadModal.style.display = 'block';
});

// Function to update modal based on mode (normal upload vs attach)
function updateModalForMode() {
    const modalHeader = document.querySelector('.upload-modal-header');
    const sendButton = document.getElementById('sendBtn');
    const draftButton = document.getElementById('saveAsDraftBtn');

    if (currentAttachMode) {
        modalHeader.innerHTML = `Attach to Letter - ${attachToLetterNo} <span class="close" id="closeModal">&times;</span>`;
        sendButton.textContent = 'Attach';
        draftButton.style.display = 'none';
    } else {
        modalHeader.innerHTML = `Upload Letter <span class="close" id="closeModal">&times;</span>`;
        sendButton.textContent = 'Send';
        draftButton.style.display = 'inline-block';
    }

    // Re-attach close event listener since we changed the HTML
    document.getElementById('closeModal').addEventListener('click', closeUploadModal);
}

// Close upload modal
function closeUploadModal() {
    uploadModal.style.display = 'none';
    document.getElementById('uploadForm').reset();
    attachmentsList.innerHTML = '';
    currentAttachMode = false;
    attachToLetterNo = null;
}

closeModal.addEventListener('click', closeUploadModal);
cancelBtn.addEventListener('click', closeUploadModal);

// Close modal when clicking outside
window.addEventListener('click', function (event) {
    if (event.target === uploadModal) {
        closeUploadModal();
    }
    if (event.target === letterDetailsModal) {
        closeLetterDetailsModal();
    }
});

// Add attachment functionality for upload modal
addAttachmentBtn.addEventListener('click', function () {
    attachmentInput.click();
});

attachmentInput.addEventListener('change', function (e) {
    const files = Array.from(e.target.files);
    files.forEach(file => {
        addAttachmentToList(file);
    });
});

function addAttachmentToList(file) {
    const attachmentItem = document.createElement('div');
    attachmentItem.className = 'attachment-item';

    const fileExtension = file.name.split('.').pop().toUpperCase();
    const fileSize = (file.size / 1024).toFixed(1) + ' KB';

    attachmentItem.innerHTML = `
        <div class="attachment-icon">${fileExtension}</div>
        <div class="attachment-info">
            <div class="attachment-name">${file.name}</div>
            <div class="attachment-size">${fileSize}</div>
        </div>
        <button type="button" class="remove-attachment" onclick="removeAttachment(this)">×</button>
    `;

    attachmentsList.appendChild(attachmentItem);
}

function removeAttachment(button) {
    button.parentElement.remove();
}

function addLetterToTable(letterData, isDraft = false) {
    const tableBody = isDraft
        ? document.getElementById('draftTableBody')
        : document.getElementById('documentTableBody');

    if (!tableBody) {
        console.error("❌ Table body not found!");
        return;
    }

    const row = tableBody.insertRow();
    const viewPage = 'view.html';
    const corrId = letterData.correspondenceId || '';
    // File display logic
    let fileDisplay = 'No files';
    if (letterData.fileCount && letterData.fileCount > 0) {
        fileDisplay = letterData.fileCount === 1 ? (letterData.fileType || 'File')
            : `${letterData.fileCount} Files`;
    }
    const formattedDue = formatApiDate(letterData.dueDate);
    // For draft table (different structure)
    if (isDraft) {
        row.innerHTML = `
		<td>${fileDisplay}</td>
		       <td>${letterData.category || ''}</td>
			   <td>
              <a href="${viewPage}?id=${encodeURIComponent(corrId)}" class="letter-link" data-id="${corrId}">
               ${letterData.letterNumber || ''}
             </a>
                </td>
		       <td>System</td>
		       <td>${letterData.recipient || ''}</td>
		       <td>${letterData.subject || ''}</td>
		       <td>${letterData.requiredResponse || ''}</td>
		       <td>${formattedDue || ''}</td>
		       <td>${letterData.currentStatus || ''}</td>
		       <td>${letterData.department || ''}</td>
		       <td>${letterData.fileCount || ''}</td>
        `;
    } else {
        // For main table
        row.innerHTML = `
            <td>${fileDisplay}</td>
            <td>${letterData.category || ''}</td>
		<td>
              <a href="${viewPage}?id=${encodeURIComponent(corrId)}" class="letter-link" data-id="${corrId}">
               ${letterData.letterNumber || ''}
             </a>
                </td>
            <td>System</td>
            <td>${letterData.recipient || ''}</td>
            <td>${letterData.subject || ''}</td>
            <td>${letterData.requiredResponse || ''}</td>
            <td>${formattedDue || ''}</td>
            <td>${letterData.currentStatus || ''}</td>
            <td>${letterData.department || ''}</td>
            <td>${letterData.fileCount || ''}</td>
        `;
    }
}
async function loadCorrespondenceList(action) {
    try {
        const letters = await getCorrespondenceList(action);
        console.log("📌 API response:", letters);

        // Clear the appropriate table based on action
        if (action === 'Save as Draft') {
            clearTable('draftTableBody');
            letters.forEach(letter => {
                addLetterToTable(letter, true);
            });

            // Reinitialize DataTable for draft table
            $('#draftTable').DataTable().destroy();
            $('#draftTable').DataTable({
                "language": {
                    "lengthMenu": "Show _MENU_ entries",
                    "info": "Showing _START_ to _END_ of _TOTAL_ entries"
                },
                "pageLength": 10
            });
        } else {
            clearTable('documentTableBody');
            letters.forEach(letter => {
                addLetterToTable(letter, false);
            });

            // Reinitialize DataTable for main table
            $('#mainTable').DataTable().destroy();
            $('#mainTable').DataTable({
                "language": {
                    "lengthMenu": "Show _MENU_ entries",
                    "info": "Showing _START_ to _END_ of _TOTAL_ entries"
                },
                "pageLength": 10
            });
        }

    } catch (error) {
        console.error('Error loading correspondence list:', error);
    }
}



// Clear table before populating with new data
function clearTable(tableId) {
    const tableBody = document.getElementById(tableId);
    if (tableBody) {
        tableBody.innerHTML = '';
    }
}

// Get form data
function getFormData() {
    const ccValue = document.getElementById('ccField').value;
    const referenceLettersValue = document.getElementById('referenceLetters').value;

    return {
        category: document.getElementById('category').value,
        letterNumber: document.getElementById('letterNo').value,
        letterDate: document.getElementById('letterDate').value,
        to: document.getElementById('toField').value,
        cc: ccValue ? ccValue.split(',').map(item => item.trim()).filter(item => item) : [],
        referenceLetters: referenceLettersValue ? referenceLettersValue.split(',').map(item => item.trim()).filter(item => item) : [],
        subject: document.getElementById('subject').value,
        keyInformation: document.getElementById('keyInformation').value,
        requiredResponse: document.getElementById('requiredResponse').value,
        currentStatus: document.getElementById('currentStatus').value,
        department: document.getElementById('department').value,
        dueDate: document.getElementById('dueDate').value,
        action: 'upload'
    };
}

// Get attachments from form
function getAttachmentsFromForm() {
    return Array.from(attachmentInput.files);
}

// Validate form with email validation
function validateForm(formData) {
    if (!formData.letterNumber) {
        alert('Please enter a Letter Number');
        return false;
    }

    return true;
}

// API call to upload letter
async function uploadLetterToServer(formData, files) {
    try {
        // Create FormData for the request
        const requestFormData = new FormData();

        // Add the DTO as JSON
        const dto = {
            category: formData.category,
            letterNumber: formData.letterNumber,
            to: formData.to,
            cc: formData.cc,
            referenceLetters: formData.referenceLetters,
            subject: formData.subject,
            keyInformation: formData.keyInformation,
            requiredResponse: formData.requiredResponse,
            currentStatus: formData.currentStatus,
            department : formData.department,
            dueDate: formData.dueDate,
            letterDate: formData.letterDate,
            action: formData.action
        };

        console.log('Sending DTO:', dto);

        requestFormData.append('dto', JSON.stringify(dto));

        // Add files - support any file type
        files.forEach(file => {
            requestFormData.append('document', file);
        });

        // Make the API call
        const response = await fetch(`${API_BASE_URL}/uploadLetter`, {
            method: 'POST',
            body: requestFormData
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Server returned ${response.status}: ${errorText}`);
        }

        const result = await response.text();
        return result;
    } catch (error) {
        console.error('Error uploading letter:', error);
        throw error;
    }
}

// API call to get correspondence list
async function getCorrespondenceList(action) {
    try {
        const response = await fetch(`${API_BASE_URL}/getCorrespondeneceList?action=${action}`);

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Server returned ${response.status}: ${errorText}`);
        }

        const letters = await response.json();
        return letters;
    } catch (error) {
        console.error('Error fetching correspondence list:', error);
        throw error;
    }
}

// Form submission - Send/Attach
sendBtn.addEventListener('click', async function () {
    if (currentAttachMode) {
        // Handle attachment mode
        const files = getAttachmentsFromForm();
        if (files.length === 0) {
            alert('Please select at least one file to attach');
            return;
        }

        alert('Attach functionality would require additional backend implementation');
        closeUploadModal();
        loadCorrespondenceList('send');
    } else {
        // Handle normal send mode
        const formData = getFormData();
        if (validateForm(formData)) {

            formData.action = 'send';

            const files = getAttachmentsFromForm();

            try {
                // Show loading state
                sendBtn.disabled = true;
                sendBtn.textContent = 'Sending...';

                const result = await uploadLetterToServer(formData, files);
                alert('Letter uploaded successfully!');

                closeUploadModal();

                // Refresh the table - fetch fresh data from server
                await loadCorrespondenceList('Send');

                console.log('✅ All letters loaded');
            } catch (error) {
                alert('Failed to upload letter: ' + error.message);
            } finally {
                // Reset button state
                sendBtn.disabled = false;
                sendBtn.textContent = 'Send';
            }
        }
    }
});

// Save as Draft button functionality
saveAsDraftBtn.addEventListener('click', async function () {
    const formData = getFormData();

    if (!formData.letterNumber) {
        alert('Please enter at least a Letter Number');
        return;
    }

    // For drafts, we don't validate emails since they might not be ready to send

    formData.action = 'Save as Draft';

    const files = getAttachmentsFromForm();

    try {
        // Show loading state
        saveAsDraftBtn.disabled = true;
        saveAsDraftBtn.textContent = 'Saving...';

        const result = await uploadLetterToServer(formData, files);
        alert('Draft saved successfully!');

        closeUploadModal();

        // Refresh the table - fetch fresh data from server
        loadCorrespondenceList('all');
    } catch (error) {
        alert('Failed to save draft: ' + error.message);
    } finally {
        // Reset button state
        saveAsDraftBtn.disabled = false;
        saveAsDraftBtn.textContent = 'Save as Draft';
    }
});


async function loadCorrespondenceList(action) {
    try {
        const letters = await getCorrespondenceList(action);

        console.log("📌 API response:", letters);   // Debugging

        // Clear the appropriate table based on action
        if (action === 'Save as Draft') {
            clearTable('draftTableBody');
            letters.forEach(letter => {
                addLetterToTable(letter, true); // true means add to draft table
            });
        } else {
            clearTable('documentTableBody');
            letters.forEach(letter => {
                addLetterToTable(letter, false); // false means add to main table
            });
        }

    } catch (error) {
        console.error('Error loading correspondence list:', error);
    }
}


// Update draft count
function updateDraftCount() {
    const draftTableBody = document.getElementById('draftTableBody');
    const count = draftTableBody ? draftTableBody.rows.length : 0;
    const draftCountElement = document.getElementById('draftCount');
    if (draftCountElement) {
        draftCountElement.textContent = count;
    }
}

// Show draft table - FIXED
document.getElementById('draftBtn').addEventListener('click', function () {
    document.getElementById('mainTableContainer').style.display = 'none';
    document.getElementById('draftTableContainer').style.display = 'block';
    document.getElementById('backToMainBtn').style.display = 'inline-block';

    // Load draft correspondence list
    loadCorrespondenceList('Save as Draft'); // Changed from 'all' to 'Save as Draft'
});

// Show main table
document.getElementById('backToMainBtn').addEventListener('click', function () {
    document.getElementById('draftTableContainer').style.display = 'none';
    document.getElementById('mainTableContainer').style.display = 'block';
    this.style.display = 'none';
});

// Open letter details modal
function openLetterDetails(letterNo) {
    const data = lettersData[letterNo];
    if (!data) {
        alert('Letter data not found');
        return;
    }

    // Fill form with data
    document.getElementById('modalLetterNo').textContent = letterNo;
    document.getElementById('detailCategory').value = data.category || '';
    document.getElementById('detailStatus').value = data.currentStatus || '';
    document.getElementById('detailLetterNo').value = data.letterNumber || '';
    document.getElementById('detailLetterDate').value = data.letterDate || '';
    document.getElementById('detailFromField').value = data.fromField || '';
    document.getElementById('detailToField').value = data.to || '';
    document.getElementById('detailCcField').value = Array.isArray(data.cc) ? data.cc.join(', ') : '';
    document.getElementById('detailDepartment').value = data.department || '';

    // Load reference letters
    loadReferenceLetters(Array.isArray(data.referenceLetters) ? data.referenceLetters : []);

    document.getElementById('detailSubject').value = data.subject || '';
    document.getElementById('detailKeyInformation').value = data.keyInformation || '';
    document.getElementById('detailRequiredResponse').value = data.requiredResponse || '';
    document.getElementById('detailDueDate').value = data.dueDate || '';

    // Load attachments for viewing only - UPDATED FOR ANY FILE TYPE
    loadAttachmentsForViewing(data.documents || []);

    // Show modal
    letterDetailsModal.style.display = 'flex';
}

// Load reference letters
function loadReferenceLetters(referenceLetters) {
    const referenceLettersList = document.getElementById('referenceLettersList');
    referenceLettersList.innerHTML = '';

    if (referenceLetters.length === 0) {
        referenceLettersList.innerHTML = '<div style="color: #666; font-style: italic;">No reference letters</div>';
        return;
    }

    referenceLetters.forEach((refLetter, index) => {
        const referenceItem = document.createElement('div');
        referenceItem.className = 'reference-letter-item';
        referenceItem.innerHTML = `
            <span class="reference-letter-number">${index + 1}.</span>
            <span class="reference-letter-link" onclick="openReferenceLetterDetails('${refLetter}')">${refLetter}</span>
        `;
        referenceLettersList.appendChild(referenceItem);
    });
}

// Load attachments for viewing only - UPDATED FOR ANY FILE TYPE
function loadAttachmentsForViewing(attachments) {
    const seeAttachmentsList = document.getElementById('seeAttachmentsList');
    seeAttachmentsList.innerHTML = '';

    if (attachments.length === 0) {
        seeAttachmentsList.innerHTML = '<div style="color: #666; font-style: italic;">No attachments</div>';
        return;
    }

    attachments.forEach(attachment => {
        const fileName = attachment.name || attachment.fileName || 'Unknown file';
        const fileExtension = fileName.split('.').pop().toUpperCase();
        const fileSize = attachment.size ? (attachment.size / 1024).toFixed(1) + ' KB' : 'Unknown size';

        const attachmentItem = document.createElement('div');
        attachmentItem.className = 'attachment-view-item';
        attachmentItem.innerHTML = `
            <div class="attachment-icon">${fileExtension}</div>
            <div class="attachment-info">
                <div class="attachment-name">${fileName}</div>
                <div class="attachment-size">${fileSize}</div>
            </div>
            <button type="button" class="view-attachment" onclick="viewAttachment('${fileName}')">View</button>
        `;
        seeAttachmentsList.appendChild(attachmentItem);
    });
}

// Open reference letter details
function openReferenceLetterDetails(refLetterNo) {
    alert('Opening reference letter: ' + refLetterNo);
    // In real implementation, this would open the reference letter details
}

// Close letter details modal
function closeLetterDetailsModal() {
    letterDetailsModal.style.display = 'none';
}

closeLetterModal.addEventListener('click', closeLetterDetailsModal);
cancelDetailBtn.addEventListener('click', closeLetterDetailsModal);

// Attach button functionality
attachBtn.addEventListener('click', function () {
    const letterNo = document.getElementById('detailLetterNo').value;

    // Close the letter details modal
    closeLetterDetailsModal();

    // Set attach mode
    currentAttachMode = true;
    attachToLetterNo = letterNo;

    // Open upload modal in attach mode
    setTimeout(() => {
        updateModalForMode();
        uploadModal.style.display = 'block';

        // Clear form but keep it disabled for attachment mode
        document.getElementById('uploadForm').reset();

        // Disable form fields in attach mode
        const formElements = document.querySelectorAll('#uploadForm input, #uploadForm select, #uploadForm textarea');
        formElements.forEach(element => {
            if (element.id !== 'attachmentInput') {
                element.disabled = currentAttachMode;
            }
        });
    }, 100);
});

// Reply button functionality
replyBtn.addEventListener('click', function () {
    const letterNo = document.getElementById('detailLetterNo').value;
    const fromField = document.getElementById('detailFromField').value;
    const subject = document.getElementById('detailSubject').value;

    // Close current modal
    closeLetterDetailsModal();

    // Set normal mode
    currentAttachMode = false;
    attachToLetterNo = null;

    // Open upload modal with pre-filled reply data
    setTimeout(() => {
        updateModalForMode();
        uploadModal.style.display = 'block';

        // Pre-fill reply data
        document.getElementById('toField').value = fromField;
        document.getElementById('subject').value = 'RE: ' + subject;
        document.getElementById('referenceLetters').value = letterNo;

        // Auto-generate reply letter number
        const replyLetterNo = 'RE-' + letterNo + '-' + String(Date.now()).slice(-4);
        document.getElementById('letterNo').value = replyLetterNo;

        // Set current date
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('letterDate').value = today;

        // Enable all form fields for reply mode
        const formElements = document.querySelectorAll('#uploadForm input, #uploadForm select, #uploadForm textarea');
        formElements.forEach(element => {
            element.disabled = false;
        });
    }, 100);
});

// View attachment
function viewAttachment(fileName) {
    alert('Opening attachment: ' + fileName);
    // In real implementation, this would open/download the file
}

// Make functions global
window.openLetterDetails = openLetterDetails;
window.viewAttachment = viewAttachment;
window.openReferenceLetterDetails = openReferenceLetterDetails;
window.removeAttachment = removeAttachment;

// Load correspondence list on page load
//document.addEventListener('DOMContentLoaded', function() {
//    loadCorrespondenceList('all');
//});
document.addEventListener('DOMContentLoaded', function() {
    loadCorrespondenceList('send');   // ✅ सही
});


// Sidebar navigation with page redirect
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

// Search and Filter functionality
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
        "language": {
            "lengthMenu": "Show _MENU_ entries",
            "info": "Showing _START_ to _END_ of _TOTAL_ entries"
        },
        "pageLength": 10
    });
});

$(document).ready(function() {
    $('#draftTable').DataTable({
        "language": {
            "lengthMenu": "Show _MENU_ entries",
            "info": "Showing _START_ to _END_ of _TOTAL_ entries"
        },
        "pageLength": 10
    });
});

// Multi-checkbox functionality
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
                    "info": "Showing _START_ to _END of _TOTAL_ entries"
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
});
function setupAutocomplete(inputId) {
    $("#" + inputId).autocomplete({
        source: function (request, response) {
            // Extract the current search term (text after last comma)
            var currentVal = $("#" + inputId).val();
            var lastCommaIndex = currentVal.lastIndexOf(',');
            var searchTerm = lastCommaIndex === -1 ? currentVal : currentVal.substring(lastCommaIndex + 1).trim();

            $.ajax({
                url: "http://localhost:8000/dms/api/users/search",
                data: { query: searchTerm },
                success: function (data) {
                    response($.map(data, function (item) {
                        return {
                            label: item.userName + " (" + item.emailId + ")",
                            value: item.emailId,
                            email: item.emailId
                        };
                    }));
                },
                error: function (xhr) {
                    console.error("Autocomplete error:", xhr);
                }
            });
        },
        minLength: 1,
        delay: 300,
        select: function (event, ui) {
            event.preventDefault();

            var input = $("#" + inputId);
            var currentVal = input.val().trim();

            // Find the position of the last comma to determine where to insert
            var lastCommaIndex = currentVal.lastIndexOf(',');

            if (lastCommaIndex === -1) {
                // No existing emails - set the value to the selected email
                input.val(ui.item.value + ', ');
            } else {
                // Replace the text after the last comma with the selected email
                var baseEmails = currentVal.substring(0, lastCommaIndex + 1);
                input.val(baseEmails + ' ' + ui.item.value + ', ');
            }

            return false;
        },
        focus: function (event, ui) {
            event.preventDefault();
            return false;
        }
    });
}


$(document).ready(function () {
    setupAutocomplete("toField", "toEmails");
    setupAutocomplete("ccField", "ccEmails");
});

$(function () {
    function fetchSuggestions(request, response) {
        // Get the last typed token after ";"
        let terms = request.term.split(";");
        let lastTerm = terms[terms.length - 1].trim();

        if (lastTerm.length === 0) {
            return response([]);
        }

        $.ajax({
            url: "http://localhost:8000/dms/api/correspondence/getReferenceLetters",
            data: { query: lastTerm },
            success: function (data) {
                response(data);
            }
        });
    }

    $("#referenceLetters").autocomplete({
        source: fetchSuggestions,
        minLength: 1,
        focus: function () {
            return false;
        },
        select: function (event, ui) {
            let terms = this.value.split(";");

            terms.pop();

            terms.push(ui.item.value);

            terms.push("");
            this.value = terms.join("; ");
            return false;
        }
    });
});


// Function to fetch statuses from API
async function fetchStatuses() {
    try {
        const response = await fetch('http://localhost:8000/dms/api/statuses/get');

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const statuses = await response.json();
        populateStatusDropdown(statuses);

    } catch (error) {
        console.error('Error fetching statuses:', error);
        document.getElementById('currentStatus').innerHTML =
            '<option value="">Error loading statuses</option>';
    }
}

// Function to populate dropdown with statuses
function populateStatusDropdown(statuses) {
    const dropdown = document.getElementById('currentStatus');

    // Clear loading message
    dropdown.innerHTML = '';

    // Check if the API returns strings or objects
    const isObjectArray = typeof statuses[0] === 'object';

    statuses.forEach(status => {
        const statusName = status.name; // 👈 Use name
        const option = document.createElement('option');
        option.value = statusName;      // 👈 send name to backend
        option.textContent = statusName;
        dropdown.appendChild(option);
    });
}

// Call this function when your modal opens or page loads

uploadBtn.addEventListener('click', function() {
    // Your existing code
    fetchStatuses(); // Add this to load statuses when modal opens
});
// Fetch department list from API and populate dropdown
async function fetchDepartments() {
    try {
        const departmentSelect = document.getElementById('department');
        departmentSelect.innerHTML = '<option value="">Loading departments...</option>';

        const response = await fetch('http://localhost:8000/dms/api/departments/get');

        if (!response.ok) {
            throw new Error(`Server returned ${response.status}`);
        }

        const departments = await response.json();

        if (!Array.isArray(departments)) {
            throw new Error('Invalid response format: expected array');
        }

        populateDepartmentDropdown(departments);

    } catch (error) {
        console.error('Error fetching departments:', error);
        document.getElementById('department').innerHTML =
            '<option value="">Error loading departments</option>';
    }
}

// Populate dropdown with **department names as values**
function populateDepartmentDropdown(departments) {
    const departmentSelect = document.getElementById('department');
    departmentSelect.innerHTML = '';

    // Default option
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = 'Select Department';
    departmentSelect.appendChild(defaultOption);

    // Add each department as an option (use name as value)
    departments.forEach(dept => {
        const deptName = dept.name || dept.label || dept.value;

        const option = document.createElement('option');
        option.value = deptName;        // 👈 use name instead of ID
        option.textContent = deptName;

        departmentSelect.appendChild(option);
    });
}

// Call this when the page loads
fetchDepartments();


// Call this function when your modal opens
uploadBtn.addEventListener('click', function() {
    // Your existing code
    fetchDepartments(); // Add this to load departments when modal opens
});

// Also call when opening letter details
function openLetterDetails(letterNo) {
    // Your existing code
    fetchDepartments(); // Add this to load departments when opening letter details
}

// Close modal
$("#closeLetterModal, #cancelDetailBtn").on("click", function () {
    $("#letterDetailsModal").hide();
});
// ---------- Auto-trigger uploadBtn flow when arriving with ?replyTo=... ----------

// function checkAndOpen() {
//     try {
//         const qp = new URLSearchParams(window.location.search);
//         if (qp.has('replyTo') || qp.has('openUpload')) {
//             triggerUploadFlow({
//                 replyTo: qp.get('replyTo') || qp.get('id') || '',
//               //  from: qp.get('from') || '',
//               //  subject: qp.get('subject') || '',
//                 refLetter: qp.get('refLetter') || ''
//             });
//         }
//     } catch(e){ console.error('AutoOpen parse error', e); }
// }
(function(){
    function $id(id){ try { return document.getElementById(id); } catch(e){ return null; } }

    function triggerUploadFlow({ replyTo='', refLetter='', refLetters='' } = {}) {
        try {
            // Prefer the existing globals if available
            const modal = (typeof uploadModal !== 'undefined' && uploadModal) ? uploadModal : $id('uploadModal');
            const btn   = (typeof uploadBtn !== 'undefined' && uploadBtn) ? uploadBtn : $id('uploadBtn');

            // If neither modal nor button found, bail with message for debugging
            if (!modal && !btn) {
                console.error('AutoOpen: uploadModal and uploadBtn not found');
                return;
            }

            // Ensure upload mode (same as your uploadBtn handler)
            try { currentAttachMode = false; } catch(_) {}
            try { attachToLetterNo = null; } catch(_) {}

            // If your app has an updateModalForMode() helper, call it
            if (typeof updateModalForMode === 'function') {
                try { updateModalForMode(); } catch(err) { console.warn('updateModalForMode threw', err); }
            }

            // Show the modal using same mechanism your handler uses
            if (modal) {
                try { modal.style.display = 'block'; } catch(e){ console.error('Failed to display modal', e); }
            } else if (btn) {
                // fallback: simulate a click on uploadBtn
                try { btn.click(); } catch(e){ console.error('Failed to click uploadBtn', e); }
            }

            // Wait a bit for the modal to fully render before trying to set values
            setTimeout(() => {
                // Prefill reference letters field with all referenced letters
                if (refLetters && $id('referenceLetters')) {
                    // Decode the reference letters value
                    const decodedRefLetters = decodeURIComponent(refLetters);

                    // Set the reference letters field value (comma-separated)
                    $id('referenceLetters').value = decodedRefLetters;

                    console.log('Reference letters set to:', decodedRefLetters);
                }
                // Also add the single reference letter if provided
                else if (refLetter && $id('referenceLetters')) {
                    // Decode the reference letter value
                    const decodedRefLetter = decodeURIComponent(refLetter);

                    // Set the reference letters field value
                    $id('referenceLetters').value = decodedRefLetter;

                    console.log('Reference letter set to:', decodedRefLetter);
                }

                // Enable inputs in case they were disabled in some mode
                document.querySelectorAll('#uploadForm input, #uploadForm select, #uploadForm textarea')
                    .forEach(el => { try { el.disabled = false; } catch(_){} });

                // Refresh dropdowns if functions exist
                if (typeof fetchDepartments === 'function') try { fetchDepartments(); } catch(_) {}
                if (typeof fetchStatuses === 'function') try { fetchStatuses(); } catch(_) {}
            }, 300);

            // Clean URL to avoid re-open on reload
            try {
                const u = new URL(window.location.href);
                u.searchParams.delete('replyTo');
                u.searchParams.delete('from');
                u.searchParams.delete('subject');
                u.searchParams.delete('openUpload');
                u.searchParams.delete('refLetter');
                u.searchParams.delete('refLetters');
                u.searchParams.delete('replyToLetter');
                history.replaceState(null, '', u.pathname + u.search);
            } catch(_) {}
        } catch(err){
            console.error('AutoOpen: unexpected error', err);
        }
    }

    function checkAndOpen() {
        try {
            const qp = new URLSearchParams(window.location.search);
            if (qp.has('replyTo') || qp.has('openUpload') || qp.has('refLetter') || qp.has('refLetters') || qp.has('replyToLetter')) {
                console.log('Auto-opening upload modal with params:', {
                    replyTo: qp.get('replyTo'),
                    refLetter: qp.get('refLetter'),
                    refLetters: qp.get('refLetters'),
                    replyToLetter: qp.get('replyToLetter')
                });

                triggerUploadFlow({
                    replyTo: qp.get('replyTo') || qp.get('id') || '',
                    refLetter: qp.get('refLetter') || qp.get('replyToLetter') || '',
                    refLetters: qp.get('refLetters') || ''
                });
            }
        } catch(e){ console.error('AutoOpen parse error', e); }
    }
    // Wait for DOM to be fully loaded before checking URL parameters
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            // Add a small delay to ensure all elements are fully loaded
            setTimeout(checkAndOpen, 100);
        });
    } else {
        setTimeout(checkAndOpen, 100);
    }
})();


// ---------- date formatting helpers ----------
function pad(n){ return String(n).padStart(2, '0'); }

function formatApiDate(value) {
    if (!value) return '';
    // backend sometimes returns arrays like [2025,9,18,...]
    if (Array.isArray(value)) {
        const year = value[0], month = value[1], day = value[2];
        return `${pad(day)}-${pad(month)}-${year}`;
    }
    // ISO string "2025-09-18" or "2025-09-18T11:44:46..."
    if (typeof value === 'string') {
        const iso = value.split('T')[0];                // "yyyy-mm-dd"
        const parts = iso.split('-');
        if (parts.length === 3) {
            return `${parts[2]}-${parts[1]}-${parts[0]}`;
        }
        // fallback: try parsing Date
        const dt = new Date(value);
        if (!isNaN(dt)) return dt.toLocaleDateString('en-GB').replace(/\//g, '-');
        return value;
    }
    // fallback to string
    return String(value);
}

function formatApiDateTime(value) {
    if (!value) return '';
    if (Array.isArray(value)) {
        const y = value[0], m = value[1], d = value[2], hh = value[3] || 0, mm = value[4] || 0, ss = value[5] || 0;
        return `${pad(d)}-${pad(m)}-${y} ${pad(hh)}:${pad(mm)}:${pad(ss)}`;
    }
    if (typeof value === 'string') {
        // try "YYYY-MM-DDTHH:MM:SS" or "YYYY-MM-DD"
        const [datePart, timePart] = value.split('T');
        const dateParts = (datePart || '').split('-');
        if (dateParts.length === 3) {
            const dateStr = `${dateParts[2]}-${dateParts[1]}-${dateParts[0]}`;
            if (timePart) {
                const time = timePart.split('.')[0]; // remove ms
                return `${dateStr} ${time}`;
            }
            return dateStr;
        }
        const dt = new Date(value);
        if (!isNaN(dt)) return dt.toLocaleString('en-GB').replace(',', '');
        return value;
    }
    return String(value);
}

