import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getAuth, signInAnonymously } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import {
  initializeFirestore,
  persistentLocalCache,
  persistentMultipleTabManager,
  collection,
  onSnapshot,
  doc,
  setDoc,
  deleteDoc
} from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";

// Firebase Configuration for team4-13a8b
const firebaseConfig = {
  apiKey: "AIzaSyBH4kOLM2AVMnL04P9Kb5ZoA5xO5lG2hOQ",
  authDomain: "team4-13a8b.firebaseapp.com",
  projectId: "team4-13a8b",
  storageBucket: "team4-13a8b.firebasestorage.app",
  messagingSenderId: "850030871862",
  appId: "1:850030871862:web:a73fde2a2704ef38795f97"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

// Initialize Firestore with Offline IndexedDB Persistence
let db;
try {
  db = initializeFirestore(app, {
    localCache: persistentLocalCache({ tabManager: persistentMultipleTabManager() })
  });
} catch (e) {
  console.warn("Persistent cache fallback:", e);
}

// Global State
let state = {
  students: [],
  payments: [],
  expenses: [],
  clothingOrders: [],
  filter: 'ALL',
  searchQuery: '',
  activeStudentId: null
};

// Network Online / Offline Detection
function updateOnlineStatus() {
  const syncBadge = document.getElementById('syncBadge');
  const syncStatus = document.getElementById('syncStatus');
  if (navigator.onLine) {
    if (syncBadge) syncBadge.className = "flex items-center space-x-1 bg-white/20 px-3 py-1 rounded-full text-xs font-semibold";
    if (syncStatus) syncStatus.innerText = "Live Cloud Sync";
  } else {
    if (syncBadge) syncBadge.className = "flex items-center space-x-1 bg-amber-500/80 text-white px-3 py-1 rounded-full text-xs font-semibold";
    if (syncStatus) syncStatus.innerText = "Offline Mode (Cached)";
  }
}
window.addEventListener('online', updateOnlineStatus);
window.addEventListener('offline', updateOnlineStatus);
updateOnlineStatus();

// Register Service Worker for Offline App Loading
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js').then(reg => {
      console.log('ServiceWorker registered:', reg);
    }).catch(err => {
      console.log('ServiceWorker registration failed:', err);
    });
  });
}

// Authenticate Anonymously
signInAnonymously(auth).catch(err => console.error("Auth error:", err));

// Real-time Firestore Sync Listeners (Works Online & Offline)
onSnapshot(collection(db, "students"), (snapshot) => {
  state.students = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
  render();
}, (error) => console.warn("Students snapshot error:", error));

onSnapshot(collection(db, "payments"), (snapshot) => {
  state.payments = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
  render();
}, (error) => console.warn("Payments snapshot error:", error));

onSnapshot(collection(db, "expenses"), (snapshot) => {
  state.expenses = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
  render();
}, (error) => console.warn("Expenses snapshot error:", error));

onSnapshot(collection(db, "clothing_orders"), (snapshot) => {
  state.clothingOrders = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
  render();
}, (error) => console.warn("Clothing orders snapshot error:", error));

// Navigation Tab Switcher
window.switchTab = function(tabName) {
  ['students', 'expenses', 'reports'].forEach(t => {
    document.getElementById(`tab-${t}`).classList.add('hidden');
    const navBtn = document.getElementById(`nav-${t}`);
    navBtn.classList.remove('text-brand', 'font-bold');
    navBtn.classList.add('text-gray-400', 'font-semibold');
  });

  document.getElementById(`tab-${tabName}`).classList.remove('hidden');
  const activeNav = document.getElementById(`nav-${tabName}`);
  activeNav.classList.remove('text-gray-400', 'font-semibold');
  activeNav.classList.add('text-brand', 'font-bold');
};

// Filter Switcher
window.setFilter = function(filter) {
  state.filter = filter;
  ['all', 'paid', 'unpaid'].forEach(f => {
    const btn = document.getElementById(`filter-${f}`);
    if (f.toUpperCase() === filter) {
      btn.className = "flex-1 py-1.5 rounded-xl text-xs font-bold bg-brand text-white transition";
    } else {
      btn.className = "flex-1 py-1.5 rounded-xl text-xs font-bold bg-gray-100 text-gray-600 hover:bg-gray-200 transition";
    }
  });
  renderStudents();
};

// Clear Search Function
window.clearSearch = function() {
  const searchInput = document.getElementById('searchInput');
  const clearBtn = document.getElementById('clearSearchBtn');
  if (searchInput) searchInput.value = '';
  state.searchQuery = '';
  if (clearBtn) clearBtn.classList.add('hidden');
  renderStudents();
};

// Search Input Listener
document.getElementById('searchInput').addEventListener('input', (e) => {
  state.searchQuery = e.target.value.toLowerCase();
  const clearBtn = document.getElementById('clearSearchBtn');
  if (clearBtn) {
    if (state.searchQuery.length > 0) {
      clearBtn.classList.remove('hidden');
    } else {
      clearBtn.classList.add('hidden');
    }
  }
  renderStudents();
});

// Render Main Interface
function render() {
  renderStudents();
  renderExpenses();
  renderReports();
  if (state.activeStudentId) {
    renderStudentDetail(state.activeStudentId);
  }
}

function renderStudents() {
  const container = document.getElementById('studentList');
  if (!container) return;

  const sortedStudents = [...state.students].sort((a, b) => a.name.localeCompare(b.name));

  const filtered = sortedStudents.filter(s => {
    const matchesSearch = s.name.toLowerCase().includes(state.searchQuery);
    const paidAmount = state.payments.filter(p => p.studentId === s.id).reduce((sum, p) => sum + (p.amount || 0), 0);
    const isPaid = paidAmount > 0;

    if (state.filter === 'PAID') return matchesSearch && isPaid;
    if (state.filter === 'UNPAID') return matchesSearch && !isPaid;
    return matchesSearch;
  });

  if (filtered.length === 0) {
    container.innerHTML = `<div class="text-center py-10 text-gray-400 text-sm">No students found</div>`;
    return;
  }

  container.innerHTML = filtered.map(s => {
    const paidAmount = state.payments.filter(p => p.studentId === s.id).reduce((sum, p) => sum + (p.amount || 0), 0);
    const isPaid = paidAmount > 0;
    const badgeClass = isPaid ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800';
    const badgeText = isPaid ? 'Paid' : 'Unpaid';

    return `
      <div onclick="openStudentDetail('${s.id}')" class="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 flex items-center justify-between cursor-pointer hover:border-blue-200 transition">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-full bg-blue-100 text-brand font-bold flex items-center justify-center text-sm">
            ${s.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()}
          </div>
          <div>
            <h4 class="font-bold text-gray-900 text-sm">${s.name}</h4>
            <div class="flex items-center space-x-2 mt-0.5">
              <span class="text-xs px-2 py-0.5 rounded-full font-bold ${badgeClass}">${badgeText}</span>
              ${isPaid ? `<span class="text-xs text-green-700 font-bold">₱${paidAmount}</span>` : ''}
            </div>
          </div>
        </div>
        <div class="flex items-center space-x-1" onclick="event.stopPropagation()">
          <button onclick="promptPayment('${s.id}', '${escapeQuotes(s.name)}')" class="p-2 rounded-xl bg-blue-50 text-brand text-xs font-bold hover:bg-blue-100">
            + Payment
          </button>
          <button onclick="promptClothing('${s.id}', '${escapeQuotes(s.name)}')" class="p-2 rounded-xl bg-purple-50 text-accent text-xs font-bold hover:bg-purple-100">
            + Order
          </button>
        </div>
      </div>
    `;
  }).join('');
}

// Student Detail Overlay Screen (Matching Android StudentDetailScreen)
window.openStudentDetail = function(studentId) {
  state.activeStudentId = studentId;
  renderStudentDetail(studentId);
  openModal('studentDetailModal');
};

function renderStudentDetail(studentId) {
  const student = state.students.find(s => s.id === studentId);
  if (!student) return;

  const payments = state.payments.filter(p => p.studentId === studentId);
  const totalPaid = payments.reduce((sum, p) => sum + (p.amount || 0), 0);
  const isPaid = totalPaid > 0;

  const clothing = state.clothingOrders.filter(o => o.studentId === studentId);

  // Update Header Colors & Name
  const header = document.getElementById('detailHeader');
  if (isPaid) {
    header.className = "bg-green-700 text-white pt-6 pb-16 px-4 relative transition-colors";
  } else {
    header.className = "bg-red-700 text-white pt-6 pb-16 px-4 relative transition-colors";
  }

  const initials = student.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  document.getElementById('detailAvatar').innerText = initials;
  document.getElementById('detailStudentName').innerText = student.name;
  document.getElementById('detailStatusBadge').innerText = isPaid ? 'Paid' : 'Unpaid';
  document.getElementById('detailTotalPaid').innerText = `₱${totalPaid}`;

  // Action Buttons
  document.getElementById('detailAddPaymentBtn').onclick = () => promptPayment(student.id, student.name);
  document.getElementById('detailAddClothingBtn').onclick = () => promptClothing(student.id, student.name);

  // Render Payment History List
  const paymentsContainer = document.getElementById('detailPaymentsList');
  if (payments.length === 0) {
    paymentsContainer.innerHTML = `<p class="text-xs text-gray-400 py-3 text-center font-medium">No payments recorded yet.</p>`;
  } else {
    paymentsContainer.innerHTML = payments.sort((a, b) => (b.date || 0) - (a.date || 0)).map(p => `
      <div class="bg-indigo-50/60 p-3 rounded-2xl border border-indigo-100 flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="w-8 h-8 rounded-full bg-brand text-white font-bold text-xs flex items-center justify-center">₱</div>
          <div>
            <p class="font-black text-gray-900 text-base">₱${p.amount}</p>
            <p class="text-xs text-gray-400">${p.date ? new Date(p.date).toLocaleDateString() : 'Recorded'}</p>
          </div>
        </div>
        <button onclick="deletePayment('${p.id}')" class="text-gray-400 hover:text-red-500 p-1">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
        </button>
      </div>
    `).join('');
  }

  // Render Clothing Orders List
  const clothingContainer = document.getElementById('detailClothingList');
  if (clothing.length === 0) {
    clothingContainer.innerHTML = `<p class="text-xs text-gray-400 py-3 text-center font-medium">No clothing orders yet.</p>`;
  } else {
    clothingContainer.innerHTML = clothing.sort((a, b) => (b.date || 0) - (a.date || 0)).map(o => `
      <div class="bg-purple-50 p-4 rounded-2xl border border-purple-200 space-y-2">
        <div class="flex items-center justify-between border-b border-purple-200 pb-2">
          <div class="flex items-center space-x-2">
            <span class="text-lg">👕</span>
            <span class="font-bold text-purple-900 text-sm">${o.itemType}</span>
          </div>
          <span class="font-black text-purple-900 text-sm">₱${o.price}</span>
        </div>
        <div class="flex items-center justify-between text-xs text-gray-800 pt-1">
          <div class="flex items-center space-x-3 font-bold">
            ${o.customName ? `<span>Name: ${o.customName}</span>` : ''}
            ${o.customNumber ? `<span>#${o.customNumber}</span>` : ''}
            <span>Size: ${o.size || 'L'}</span>
          </div>
          <button onclick="deleteClothingOrder('${o.id}')" class="text-purple-400 hover:text-red-500 p-1">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
          </button>
        </div>
      </div>
    `).join('');
  }
}

function renderExpenses() {
  const totalCollected = state.payments.reduce((sum, p) => sum + (p.amount || 0), 0);
  const totalExpenses = state.expenses.reduce((sum, e) => sum + (e.amount || 0), 0);
  const balance = totalCollected - totalExpenses;

  document.getElementById('expenseTotalCollected').innerText = `₱${totalCollected}`;
  document.getElementById('expenseTotalExpenses').innerText = `₱${totalExpenses}`;
  document.getElementById('expenseCurrentBalance').innerText = `₱${balance}`;

  const container = document.getElementById('expenseList');
  if (!container) return;

  if (state.expenses.length === 0) {
    container.innerHTML = `<div class="text-center py-8 text-gray-400 text-sm">No expenses recorded yet</div>`;
    return;
  }

  const sortedExpenses = [...state.expenses].sort((a, b) => (b.date || 0) - (a.date || 0));

  container.innerHTML = sortedExpenses.map(e => `
    <div class="bg-white p-3.5 rounded-2xl shadow-sm border border-gray-100 flex items-center justify-between">
      <div>
        <p class="font-bold text-gray-900 text-sm">${e.description || 'Expense'}</p>
        <p class="text-xs text-gray-400">${e.date ? new Date(e.date).toLocaleDateString() : 'Today'}</p>
      </div>
      <div class="flex items-center space-x-2">
        <span class="font-black text-red-600 text-sm">₱${e.amount || 0}</span>
        <button onclick="deleteExpense('${e.id}')" class="text-gray-300 hover:text-red-500 p-1">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
        </button>
      </div>
    </div>
  `).join('');
}

function renderReports() {
  const totalCollected = state.payments.reduce((sum, p) => sum + (p.amount || 0), 0);
  const clothingTotal = state.clothingOrders.reduce((sum, o) => sum + (o.price || 0), 0);
  const totalExpenses = state.expenses.reduce((sum, e) => sum + (e.amount || 0), 0);
  const balance = totalCollected - totalExpenses;

  document.getElementById('repTotalCollected').innerText = `₱${totalCollected}`;
  document.getElementById('repClothingTotal').innerText = `₱${clothingTotal}`;
  document.getElementById('repTotalExpenses').innerText = `₱${totalExpenses}`;
  document.getElementById('repBalance').innerText = `₱${balance}`;

  const paidCount = state.students.filter(s => state.payments.some(p => p.studentId === s.id && p.amount > 0)).length;
  const unpaidCount = state.students.length - paidCount;

  document.getElementById('statusPaidCount').innerText = paidCount;
  document.getElementById('statusUnpaidCount').innerText = unpaidCount;

  // Render Tshirt / Jersey Orders Roster
  const rosterSubtitle = document.getElementById('webClothingRosterSubtitle');
  if (rosterSubtitle) {
    rosterSubtitle.innerText = `${state.clothingOrders.length} students availed (Total: ₱${clothingTotal})`;
  }

  const rosterCount = document.getElementById('clothingRosterCount');
  if (rosterCount) {
    rosterCount.innerText = `${state.clothingOrders.length} Availed • Total: ₱${clothingTotal}`;
  }

  const rosterContainer = document.getElementById('clothingRosterList');
  if (rosterContainer) {
    if (state.clothingOrders.length === 0) {
      rosterContainer.innerHTML = `<p class="text-xs text-gray-400 py-6 text-center font-medium">No t-shirt or jersey orders recorded yet.</p>`;
    } else {
      rosterContainer.innerHTML = [...state.clothingOrders].sort((a, b) => (b.date || 0) - (a.date || 0)).map(o => {
        const studentName = (state.students.find(s => s.id === o.studentId) || {}).name || o.customName || 'Student';
        return `
          <div class="bg-purple-50 p-3.5 rounded-2xl border border-purple-200 space-y-2">
            <div class="flex items-center justify-between border-b border-purple-200 pb-1.5">
              <span class="font-bold text-purple-950 text-sm">${studentName}</span>
              <span class="font-black text-purple-900 text-sm">₱${o.price || 0}</span>
            </div>
            <div class="flex items-center justify-between text-xs text-gray-700">
              <div class="font-semibold space-x-2">
                <span class="bg-purple-200 text-purple-900 px-2 py-0.5 rounded-md font-bold">${o.itemType || 'Jersey'}</span>
                ${o.customName ? `<span>Name: ${o.customName}</span>` : ''}
                ${o.customNumber ? `<span>#${o.customNumber}</span>` : ''}
                <span>Size: ${o.size || 'L'}</span>
              </div>
              <button onclick="deleteClothingOrder('${o.id}')" class="text-purple-400 hover:text-red-500 p-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
              </button>
            </div>
          </div>
        `;
      }).join('');
    }
  }
}

// Modal Helper Functions
window.openModal = function(id) {
  document.getElementById(id).classList.remove('hidden');
};

window.closeModal = function(id) {
  document.getElementById(id).classList.add('hidden');
  if (id === 'studentDetailModal') {
    state.activeStudentId = null;
  }
};

window.promptPayment = function(id, name) {
  document.getElementById('paymentStudentId').value = id;
  document.getElementById('paymentStudentName').innerText = `Add Payment for ${name}`;
  document.getElementById('paymentAmount').value = '';
  openModal('addPaymentModal');
};

window.promptClothing = function(id, name) {
  document.getElementById('clothingStudentId').value = id;
  document.getElementById('clothingStudentName').innerText = `Clothing Order for ${name}`;
  document.getElementById('clothingCustomName').value = name.split(' ')[0] || '';
  document.getElementById('clothingPrice').value = '';
  openModal('addClothingModal');
};

// CRUD Action Functions connected to Firestore (Non-blocking for Instant Offline Responsiveness)
window.submitPayment = function() {
  const studentId = document.getElementById('paymentStudentId').value;
  const amount = parseFloat(document.getElementById('paymentAmount').value) || 0;
  if (!studentId || amount <= 0) return;

  const paymentId = crypto.randomUUID();
  const paymentObj = {
    id: paymentId,
    studentId: studentId,
    amount: amount,
    date: Date.now()
  };

  // Optimistic local update & non-blocking setDoc
  state.payments.push(paymentObj);
  render();

  setDoc(doc(db, "payments", paymentId), paymentObj).catch(e => console.warn("Offline queued write:", e));
  closeModal('addPaymentModal');
};

window.submitClothingOrder = function() {
  const studentId = document.getElementById('clothingStudentId').value;
  const itemType = document.getElementById('clothingItemType').value || 'Jersey';
  const customName = document.getElementById('clothingCustomName').value || '';
  const customNumber = document.getElementById('clothingCustomNumber').value || '';
  const size = document.getElementById('clothingSize').value || 'L';
  const price = parseFloat(document.getElementById('clothingPrice').value) || 0;

  if (!studentId || price <= 0) return;

  const orderId = crypto.randomUUID();
  const orderObj = {
    id: orderId,
    studentId: studentId,
    itemType: itemType,
    description: '',
    customName: customName,
    customNumber: customNumber,
    size: size,
    price: price,
    date: Date.now()
  };

  // Optimistic local update & non-blocking setDoc
  state.clothingOrders.push(orderObj);
  render();

  setDoc(doc(db, "clothing_orders", orderId), orderObj).catch(e => console.warn("Offline queued write:", e));
  closeModal('addClothingModal');
};

window.submitExpense = function() {
  const desc = document.getElementById('expenseDescription').value || 'Expense';
  const amount = parseFloat(document.getElementById('expenseAmount').value) || 0;
  if (amount <= 0) return;

  const expenseId = crypto.randomUUID();
  const expenseObj = {
    id: expenseId,
    description: desc,
    amount: amount,
    date: Date.now()
  };

  // Optimistic local update & non-blocking setDoc
  state.expenses.push(expenseObj);
  render();

  setDoc(doc(db, "expenses", expenseId), expenseObj).catch(e => console.warn("Offline queued write:", e));
  closeModal('addExpenseModal');
};

window.deleteExpense = function(id) {
  if (confirm("Delete this expense?")) {
    state.expenses = state.expenses.filter(e => e.id !== id);
    render();
    deleteDoc(doc(db, "expenses", id)).catch(e => console.warn("Offline delete queued:", e));
  }
};

window.deletePayment = function(id) {
  if (confirm("Delete this payment entry?")) {
    state.payments = state.payments.filter(p => p.id !== id);
    render();
    deleteDoc(doc(db, "payments", id)).catch(e => console.warn("Offline delete queued:", e));
  }
};

window.deleteClothingOrder = function(id) {
  if (confirm("Delete this clothing order?")) {
    state.clothingOrders = state.clothingOrders.filter(o => o.id !== id);
    render();
    deleteDoc(doc(db, "clothing_orders", id)).catch(e => console.warn("Offline delete queued:", e));
  }
};

// Web Export Generator (Excel .csv & PDF)
window.generateWebExport = function() {
  const categories = document.getElementsByName('exportCategory');
  let selectedCategory = 'FULL_REPORT';
  for (let r of categories) { if (r.checked) { selectedCategory = r.value; break; } }

  const formats = document.getElementsByName('exportFormat');
  let selectedFormat = 'EXCEL';
  for (let r of formats) { if (r.checked) { selectedFormat = r.value; break; } }

  if (selectedFormat === 'EXCEL') {
    generateExcelExport(selectedCategory);
  } else {
    generatePdfExport(selectedCategory);
  }

  closeModal('exportModal');
};

function generateExcelExport(category) {
  const dateStr = new Date().toISOString().split('T')[0];
  const regularStudents = [...state.students].filter(s => !(s.name || '').toLowerCase().includes('borrow funds')).sort((a, b) => a.name.localeCompare(b.name));
  const borrowStudent = state.students.find(s => (s.name || '').toLowerCase().includes('borrow funds'));
  const sortedStudents = borrowStudent ? [...regularStudents, borrowStudent] : regularStudents;
  const sortedExpenses = [...state.expenses].sort((a, b) => (a.date || 0) - (b.date || 0));

  const totalColl = state.payments.reduce((sum, p) => sum + (p.amount || 0), 0);
  const clothingTotal = state.clothingOrders.reduce((sum, o) => sum + (o.price || 0), 0);
  const totalExp = state.expenses.reduce((sum, e) => sum + (e.amount || 0), 0);
  const balance = totalColl - totalExp;
  const unpaidStudentsCount = regularStudents.filter(s => {
    const paid = state.payments.filter(p => p.studentId === s.id).reduce((sum, p) => sum + (p.amount || 0), 0);
    return paid <= 0;
  }).length;

  let html = `
    <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
    <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <style>
      body { font-family: Arial, sans-serif; font-size: 11pt; }
      .title { font-size: 16pt; font-weight: bold; color: #1F4E78; margin-bottom: 10px; }
      .subtitle { font-size: 10pt; color: #595959; margin-bottom: 15px; }
      .section-header { font-size: 12pt; font-weight: bold; color: #1F4E78; margin-top: 20px; margin-bottom: 8px; }
      table { border-collapse: collapse; margin-bottom: 20px; }
      th { background-color: #1F4E78; color: #FFFFFF; font-weight: bold; border: 1px solid #0D233A; padding: 10px 20px; text-align: left; white-space: nowrap; }
      td { border: 1px solid #D9D9D9; padding: 8px 20px; vertical-align: middle; white-space: nowrap; }
      .col-name { min-width: 300px; }
      .col-payment { min-width: 140px; text-align: right; }
      tr.paid { background-color: #D4EDDA; font-weight: bold; color: #155724; }
      tr.unpaid { background-color: #FFFFFF; }
      tr.borrow-funds { background-color: #FFE0B2; font-weight: bold; color: #E65100; }
      tr.footer-success { background-color: #D4EDDA; font-weight: bold; color: #155724; }
      tr.footer-danger { background-color: #FFEBEE; font-weight: bold; color: #C62828; }
      tr.footer-purple { background-color: #F3E5F5; font-weight: bold; color: #4A148C; }
      .number { text-align: right; }
      .bold { font-weight: bold; }
    </style>
    </head>
    <body>
    <div class="title">TEAM 4</div>
    <div class="subtitle">Generated: ${dateStr}</div>
  `;

  if (category === 'FULL_REPORT' || category === 'STUDENTS_PAYMENTS') {
    html += `
      <div class="section-header">STUDENTS & PAYMENTS</div>
      <table>
        <thead><tr><th class="col-name">Full Name</th><th class="col-payment">PAYMENT</th></tr></thead>
        <tbody>
    `;
    sortedStudents.forEach(s => {
      const isBorrow = (s.name || '').toLowerCase().includes('borrow funds');
      const paid = state.payments.filter(p => p.studentId === s.id).reduce((sum, p) => sum + (p.amount || 0), 0);
      const isPaid = paid > 0;
      const rowClass = isBorrow ? 'borrow-funds' : (isPaid ? 'paid' : 'unpaid');
      html += `<tr class="${rowClass}"><td>${s.name}</td><td class="number">${isPaid ? paid : 'Unpaid'}</td></tr>`;
    });
    html += `
        </tbody>
        <tfoot>
          <tr class="footer-success"><td>TOTAL COLLECTED</td><td class="number">₱${totalColl}</td></tr>
          <tr class="footer-danger"><td>UNPAID STUDENTS (Hindi pa nagbabayad)</td><td class="number">${unpaidStudentsCount} student(s)</td></tr>
        </tfoot>
      </table>
    `;
  }

  if (category === 'FULL_REPORT' || category === 'CLOTHING_ORDERS') {
    html += `
      <div class="section-header">CLOTHING ORDERS</div>
      <table>
        <thead><tr><th class="col-name">Student Name</th><th>type</th><th>name to print</th><th>number to print</th><th>size</th><th class="number">payment</th></tr></thead>
        <tbody>
    `;
    state.clothingOrders.forEach(o => {
      const studentName = (state.students.find(s => s.id === o.studentId) || {}).name || o.customName || '';
      html += `<tr><td>${studentName}</td><td>${o.itemType || 'Jersey'}</td><td>${o.customName || ''}</td><td>${o.customNumber || ''}</td><td>${o.size || 'L'}</td><td class="number">₱${o.price || 0}</td></tr>`;
    });
    html += `
        </tbody>
        <tfoot>
          <tr class="footer-purple"><td colspan="5">TOTAL CLOTHING ORDERS</td><td class="number">₱${clothingTotal}</td></tr>
        </tfoot>
      </table>
    `;
  }

  if (category === 'FULL_REPORT' || category === 'EXPENSES_SUMMARY') {
    html += `
      <div class="section-header">EXPENSES</div>
      <table>
        <thead><tr><th>DATE</th><th>DESCRIPTION/CATEGORY</th><th class="number">AMOUNT</th></tr></thead>
        <tbody>
    `;
    sortedExpenses.forEach(e => {
      const d = e.date ? new Date(e.date).toLocaleDateString() : dateStr;
      html += `<tr><td>${d}</td><td>${e.description || ''}</td><td class="number">₱${e.amount || 0}</td></tr>`;
    });
    html += `
        </tbody>
        <tfoot>
          <tr class="footer-danger"><td colspan="2">TOTAL EXPENSES</td><td class="number">₱${totalExp}</td></tr>
        </tfoot>
      </table>
      <div class="section-header">NET FINANCIAL SUMMARY</div>
      <table>
        <thead><tr><th>FINANCIAL CATEGORY</th><th class="number">AMOUNT</th></tr></thead>
        <tbody>
          <tr><td>Total Collected from Students</td><td class="number bold" style="color: #155724;">₱${totalColl}</td></tr>
          <tr><td>Total Order for Tshirt/Jersey</td><td class="number bold" style="color: #4A148C;">₱${clothingTotal}</td></tr>
          <tr><td>Total Expenses</td><td class="number bold" style="color: #C62828;">₱${totalExp}</td></tr>
          <tr class="footer-success"><td>NET REMAINING BALANCE (Total Collected - Expenses)</td><td class="number bold">₱${balance}</td></tr>
        </tbody>
      </table>
    `;
  }

  html += `</body></html>`;

  const blob = new Blob([html], { type: 'application/vnd.ms-excel;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `TEAM_4_Report_${category.toLowerCase()}_${dateStr}.xls`;
  a.click();
  URL.revokeObjectURL(url);
}

function generatePdfExport(category) {
  const { jsPDF } = window.jspdf;
  const doc = new jsPDF();
  const dateStr = new Date().toLocaleDateString();
  const regularStudents = [...state.students].filter(s => !(s.name || '').toLowerCase().includes('borrow funds')).sort((a, b) => a.name.localeCompare(b.name));
  const borrowStudent = state.students.find(s => (s.name || '').toLowerCase().includes('borrow funds'));
  const sortedStudents = borrowStudent ? [...regularStudents, borrowStudent] : regularStudents;
  const sortedExpenses = [...state.expenses].sort((a, b) => (a.date || 0) - (b.date || 0));

  const totalColl = state.payments.reduce((sum, p) => sum + (p.amount || 0), 0);
  const clothingTotal = state.clothingOrders.reduce((sum, o) => sum + (o.price || 0), 0);
  const totalExp = state.expenses.reduce((sum, e) => sum + (e.amount || 0), 0);
  const balance = totalColl - totalExp;
  const unpaidStudentsCount = regularStudents.filter(s => {
    const paid = state.payments.filter(p => p.studentId === s.id).reduce((sum, p) => sum + (p.amount || 0), 0);
    return paid <= 0;
  }).length;

  // Title Banner
  doc.setFillColor(31, 78, 120);
  doc.rect(0, 0, 210, 20, 'F');
  doc.setTextColor(255, 255, 255);
  doc.setFontSize(16);
  doc.text("TEAM 4 - Report", 14, 13);
  doc.setFontSize(9);
  doc.text(`Generated: ${dateStr}`, 160, 13);

  let startY = 28;

  if (category === 'FULL_REPORT' || category === 'STUDENTS_PAYMENTS') {
    doc.setFontSize(11);
    doc.setTextColor(31, 78, 120);
    doc.text("STUDENTS & PAYMENTS", 14, startY);

    const studentRows = sortedStudents.map(s => {
      const paid = state.payments.filter(p => p.studentId === s.id).reduce((sum, p) => sum + (p.amount || 0), 0);
      return [s.name, paid > 0 ? `₱${paid}` : 'Unpaid'];
    });
    studentRows.push(['TOTAL COLLECTED', `₱${totalColl}`]);
    studentRows.push(['UNPAID STUDENTS (Hindi pa nagbabayad)', `${unpaidStudentsCount} student(s)`]);

    doc.autoTable({
      startY: startY + 4,
      head: [['Full Name', 'PAYMENT']],
      body: studentRows,
      headStyles: { fillColor: [31, 78, 120], textColor: [255, 255, 255] },
      didParseCell: function(data) {
        if (data.section === 'body') {
          const nameVal = data.row.cells[0].raw;
          const paymentVal = data.row.cells[1].raw;
          if (nameVal && nameVal.toLowerCase().includes('borrow funds')) {
            data.cell.styles.fillColor = [255, 224, 178];
            data.cell.styles.textColor = [230, 81, 0];
            data.cell.styles.fontStyle = 'bold';
          } else if (nameVal === 'TOTAL COLLECTED') {
            data.cell.styles.fillColor = [212, 237, 218];
            data.cell.styles.textColor = [21, 87, 36];
            data.cell.styles.fontStyle = 'bold';
          } else if (nameVal === 'UNPAID STUDENTS (Hindi pa nagbabayad)') {
            data.cell.styles.fillColor = [255, 235, 238];
            data.cell.styles.textColor = [198, 40, 40];
            data.cell.styles.fontStyle = 'bold';
          } else if (paymentVal !== 'Unpaid') {
            data.cell.styles.fillColor = [212, 237, 218];
            data.cell.styles.textColor = [21, 87, 36];
            data.cell.styles.fontStyle = 'bold';
          }
        }
      }
    });

    startY = doc.lastAutoTable.finalY + 12;
  }

  if (category === 'FULL_REPORT' || category === 'CLOTHING_ORDERS') {
    if (startY > 250) { doc.addPage(); startY = 20; }
    doc.setFontSize(11);
    doc.setTextColor(31, 78, 120);
    doc.text("CLOTHING ORDERS", 14, startY);

    const orderRows = state.clothingOrders.map(o => {
      const studentName = (state.students.find(s => s.id === o.studentId) || {}).name || o.customName || '';
      return [studentName, o.itemType || 'Jersey', o.customName || '', o.customNumber || '', o.size || 'L', `₱${o.price || 0}`];
    });
    orderRows.push(['TOTAL CLOTHING ORDERS', '', '', '', '', `₱${clothingTotal}`]);

    doc.autoTable({
      startY: startY + 4,
      head: [['Student Name', 'Type', 'Name to Print', 'Number', 'Size', 'Payment']],
      body: orderRows,
      headStyles: { fillColor: [31, 78, 120], textColor: [255, 255, 255] },
      didParseCell: function(data) {
        if (data.section === 'body' && data.row.cells[0].raw === 'TOTAL CLOTHING ORDERS') {
          data.cell.styles.fillColor = [243, 229, 245];
          data.cell.styles.textColor = [74, 20, 140];
          data.cell.styles.fontStyle = 'bold';
        }
      }
    });

    startY = doc.lastAutoTable.finalY + 12;
  }

  if (category === 'FULL_REPORT' || category === 'EXPENSES_SUMMARY') {
    if (startY > 220) { doc.addPage(); startY = 20; }

    doc.setFontSize(11);
    doc.setTextColor(31, 78, 120);
    doc.text("EXPENSES", 14, startY);

    const expenseRows = sortedExpenses.map(e => [
      e.date ? new Date(e.date).toLocaleDateString() : dateStr,
      e.description || 'Expense',
      `₱${e.amount || 0}`
    ]);
    expenseRows.push(['TOTAL EXPENSES', '', `₱${totalExp}`]);

    doc.autoTable({
      startY: startY + 4,
      head: [['Date', 'Description / Category', 'Amount']],
      body: expenseRows,
      headStyles: { fillColor: [31, 78, 120], textColor: [255, 255, 255] },
      didParseCell: function(data) {
        if (data.section === 'body' && data.row.cells[0].raw === 'TOTAL EXPENSES') {
          data.cell.styles.fillColor = [255, 235, 238];
          data.cell.styles.textColor = [198, 40, 40];
          data.cell.styles.fontStyle = 'bold';
        }
      }
    });

    startY = doc.lastAutoTable.finalY + 12;

    if (startY > 220) { doc.addPage(); startY = 20; }
    doc.setFontSize(11);
    doc.setTextColor(31, 78, 120);
    doc.text("NET FINANCIAL SUMMARY", 14, startY);

    const summaryRows = [
      ['Total Collected from Students', `₱${totalColl}`],
      ['Total Order for Tshirt/Jersey', `₱${clothingTotal}`],
      ['Total Expenses', `₱${totalExp}`],
      ['NET REMAINING BALANCE (Total Collected - Expenses)', `₱${balance}`]
    ];

    doc.autoTable({
      startY: startY + 4,
      head: [['Financial Category', 'Amount']],
      body: summaryRows,
      headStyles: { fillColor: [31, 78, 120], textColor: [255, 255, 255] },
      didParseCell: function(data) {
        if (data.section === 'body' && data.row.cells[0].raw.startsWith('NET REMAINING BALANCE')) {
          data.cell.styles.fillColor = [212, 237, 218];
          data.cell.styles.textColor = [21, 87, 36];
          data.cell.styles.fontStyle = 'bold';
        }
      }
    });
  }

  doc.save(`TEAM_4_Report_${category.toLowerCase()}_${new Date().getTime()}.pdf`);
}

function escapeQuotes(str) {
  return (str || '').replace(/'/g, "\\'");
}
