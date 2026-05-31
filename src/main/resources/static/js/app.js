// Configuration
const API_BASE = '/api';
const API_ENDPOINT = `${API_BASE}/shorten`;

// DOM Elements
const shortenForm = document.getElementById('shortenForm');
const longUrlInput = document.getElementById('longUrl');
const customCodeInput = document.getElementById('customCode');
const generateBtn = document.getElementById('generateBtn');
const spinner = document.getElementById('spinner');
const btnText = document.querySelector('.btn-text');

const formSection = document.getElementById('formSection');
const successSection = document.getElementById('successSection');
const errorAlert = document.getElementById('errorAlert');
const errorMessage = document.getElementById('errorMessage');

const shortUrlDisplay = document.getElementById('shortUrlDisplay');
const copyBtn = document.getElementById('copyBtn');
const openBtn = document.getElementById('openBtn');
const newUrlBtn = document.getElementById('newUrlBtn');
const toast = document.getElementById('toast');

const charCountElement = document.getElementById('charCount');
const urlError = document.getElementById('urlError');
const aliasError = document.getElementById('aliasError');

// State
let isSubmitting = false;

// Event Listeners
shortenForm.addEventListener('submit', handleFormSubmit);
longUrlInput.addEventListener('input', clearUrlError);
customCodeInput.addEventListener('input', updateCharCounter);
customCodeInput.addEventListener('input', clearAliasError);
copyBtn.addEventListener('click', copyToClipboard);
openBtn.addEventListener('click', openShortUrl);
newUrlBtn.addEventListener('click', resetForm);

// Allow Enter key to submit (but not in custom code field with shift)
longUrlInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        shortenForm.dispatchEvent(new Event('submit'));
    }
});

/**
 * Handle form submission
 */
async function handleFormSubmit(e) {
    e.preventDefault();

    // Prevent duplicate submissions
    if (isSubmitting) return;

    // Validate form
    if (!validateForm()) return;

    // Set submitting state
    isSubmitting = true;
    updateButtonState();

    try {
        const formData = {
            longUrl: longUrlInput.value.trim(),
        };

        // Add custom code only if provided
        if (customCodeInput.value.trim()) {
            formData.customCode = customCodeInput.value.trim();
        }

        // Make API request
        const response = await fetch(API_ENDPOINT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData),
        });

        const data = await response.json();

        if (!response.ok) {
            // Handle error response
            showError(data.message || 'Failed to shorten URL');
            return;
        }

        // Success - display result
        displaySuccess(data.shortUrl);

    } catch (error) {
        console.error('Error:', error);
        showError('Network error. Please check your connection and try again.');
    } finally {
        isSubmitting = false;
        updateButtonState();
    }
}

/**
 * Validate form inputs
 */
function validateForm() {
    // Clear previous errors
    clearUrlError();
    clearAliasError();
    errorAlert.classList.add('hidden');

    // Validate URL
    const url = longUrlInput.value.trim();
    if (!url) {
        showUrlError('URL is required');
        return false;
    }

    if (!isValidUrl(url)) {
        showUrlError('Please enter a valid URL (starting with http:// or https://)');
        return false;
    }

    // Validate custom code if provided
    const customCode = customCodeInput.value.trim();
    if (customCode) {
        if (customCode.length < 2) {
            showAliasError('Custom alias must be at least 2 characters');
            return false;
        }

        if (!customCode.match(/^[a-z0-9]+$/)) {
            showAliasError('Only lowercase letters and numbers allowed');
            return false;
        }
    }

    return true;
}

/**
 * Validate URL format
 */
function isValidUrl(string) {
    try {
        const url = new URL(string);
        return url.protocol === 'http:' || url.protocol === 'https:';
    } catch (_) {
        // If URL doesn't have protocol, try adding https://
        try {
            new URL('https://' + string);
            return true;
        } catch (_) {
            return false;
        }
    }
}

/**
 * Show error message
 */
function showError(message) {
    errorMessage.textContent = message;
    errorAlert.classList.remove('hidden');
    scrollToError();
}

/**
 * Show URL error
 */
function showUrlError(message) {
    urlError.textContent = message;
    longUrlInput.classList.add('error');
}

/**
 * Show alias error
 */
function showAliasError(message) {
    aliasError.textContent = message;
    customCodeInput.classList.add('error');
}

/**
 * Clear URL error
 */
function clearUrlError() {
    urlError.textContent = '';
    longUrlInput.classList.remove('error');
}

/**
 * Clear alias error
 */
function clearAliasError() {
    aliasError.textContent = '';
    customCodeInput.classList.remove('error');
}

/**
 * Scroll to error alert
 */
function scrollToError() {
    errorAlert.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/**
 * Update character counter for custom code
 */
function updateCharCounter() {
    charCountElement.textContent = customCodeInput.value.length;
}

/**
 * Update button state based on submission state
 */
function updateButtonState() {
    generateBtn.disabled = isSubmitting;
    if (isSubmitting) {
        btnText.style.display = 'none';
        spinner.style.display = 'block';
    } else {
        btnText.style.display = 'inline';
        spinner.style.display = 'none';
    }
}

/**
 * Display success section with generated short URL
 */
function displaySuccess(shortUrl) {
    shortUrlDisplay.value = shortUrl;
    openBtn.href = shortUrl;

    formSection.classList.remove('active');
    successSection.classList.add('active');

    // Select text in URL display for convenience
    shortUrlDisplay.select();
}

/**
 * Copy short URL to clipboard
 */
async function copyToClipboard() {
    const shortUrl = shortUrlDisplay.value;

    try {
        // Try modern clipboard API first
        if (navigator.clipboard && navigator.clipboard.writeText) {
            await navigator.clipboard.writeText(shortUrl);
        } else {
            // Fallback for older browsers
            shortUrlDisplay.select();
            document.execCommand('copy');
        }

        // Show toast notification
        showToast('Copied to clipboard!');
    } catch (err) {
        console.error('Failed to copy:', err);
        // Fallback: try manual selection
        try {
            shortUrlDisplay.select();
            document.execCommand('copy');
            showToast('Copied to clipboard!');
        } catch (fallbackErr) {
            showError('Failed to copy to clipboard');
        }
    }
}

/**
 * Show toast notification
 */
function showToast(message) {
    toast.textContent = message;
    toast.classList.remove('hidden');

    // Auto-hide after 3 seconds
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3000);
}

/**
 * Open short URL in new tab
 */
function openShortUrl() {
    window.open(shortUrlDisplay.value, '_blank');
}

/**
 * Reset form and return to initial state
 */
function resetForm() {
    // Clear form inputs
    shortenForm.reset();
    charCountElement.textContent = '0';

    // Clear errors
    clearUrlError();
    clearAliasError();
    errorAlert.classList.add('hidden');

    // Switch views
    successSection.classList.remove('active');
    formSection.classList.add('active');

    // Focus on long URL input
    longUrlInput.focus();

    // Reset state
    isSubmitting = false;
    updateButtonState();
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    longUrlInput.focus();
});
