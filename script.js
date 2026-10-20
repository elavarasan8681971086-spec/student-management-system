const STORAGE_KEY = "campusdesk_students_v1";
const demoStudents = [
  {key:"demo-1",id:"SRM2026001",name:"Ananya Kumar",email:"ananya@example.com",phone:"9876543210",dept:"CSE",year:"2nd Year",cgpa:"8.7",status:"Active"},
  {key:"demo-2",id:"SRM2026002",name:"Arun Prakash",email:"arun@example.com",phone:"9876501234",dept:"ECE",year:"2nd Year",cgpa:"7.9",status:"Active"},
  {key:"demo-3",id:"SRM2026003",name:"Divya S",email:"divya@example.com",phone:"9876512340",dept:"CSE",year:"1st Year",cgpa:"9.1",status:"Active"}
];
let students = loadStudents();
let toastTimer;
const $ = id => document.getElementById(id);

function loadStudents() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    return saved === null ? demoStudents : JSON.parse(saved);
  } catch { return demoStudents; }
}
function saveStudents() { localStorage.setItem(STORAGE_KEY, JSON.stringify(students)); }
function escapeHTML(value) {
  return String(value ?? "").replace(/[&<>"']/g, ch => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[ch]));
}
function initials(name) {
  return String(name || "?").trim().split(/\s+/).slice(0,2).map(part=>part[0]||"").join("").toUpperCase();
}
function studentRows(items) {
  if (!items.length) return `<div class="empty-state"><b>No students found</b>Try another search or register a student.</div>`;
  return `<div class="table-wrap"><table><thead><tr><th>Student</th><th>Department</th><th>Year</th><th>CGPA</th><th>Status</th><th>Actions</th></tr></thead><tbody>${items.map(s=>`
    <tr>
      <td><div class="student-name"><span class="avatar">${escapeHTML(initials(s.name))}</span><div>${escapeHTML(s.name)}<div class="student-id">${escapeHTML(s.id)}</div></div></div></td>
      <td>${escapeHTML(s.dept)}</td><td>${escapeHTML(s.year)}</td><td>${s.cgpa ? escapeHTML(s.cgpa) : "—"}</td>
      <td><span class="pill ${s.status==="Active"?"active":"inactive"}">${escapeHTML(s.status)}</span></td>
      <td><button class="action-btn" data-edit="${escapeHTML(s.key)}">Edit</button><button class="action-btn" data-delete="${escapeHTML(s.key)}">Delete</button></td>
    </tr>`).join("")}</tbody></table></div>`;
}
function render() {
  $("total-count").textContent = students.length;
  $("cse-count").textContent = students.filter(s=>s.dept==="CSE").length;
  $("other-count").textContent = students.filter(s=>s.dept!=="CSE").length;
  const newest = [...students].reverse().slice(0,4);
  $("recent-table").innerHTML = studentRows(newest);
  renderStudents();
}
function renderStudents() {
  const query = $("search-input").value.trim().toLowerCase();
  const dept = $("department-filter").value;
  const filtered = students.filter(s => {
    const matches = [s.name,s.id,s.email,s.dept,s.year].some(v=>String(v||"").toLowerCase().includes(query));
    return matches && (!dept || s.dept===dept);
  });
  $("students-table").innerHTML = studentRows(filtered);
}
function showView(view) {
  const titles = {dashboard:"Dashboard",students:"Students",add:"Add Student"};
  document.querySelectorAll(".view").forEach(el=>el.classList.add("hidden"));
  $(`${view}-view`).classList.remove("hidden");
  document.querySelectorAll(".nav-link").forEach(link=>link.classList.toggle("active",link.dataset.view===view));
  $("page-title").textContent = titles[view] || "Dashboard";
  if (view==="students") renderStudents();
  window.location.hash = view==="add" ? "add-student" : view;
}
function goAdd() {
  resetForm();
  showView("add");
  $("student-id").focus();
}
function resetForm() {
  $("student-form").reset();
  $("student-key").value = "";
  $("form-title").textContent = "Register a student";
  $("save-student").textContent = "Save student";
  $("form-error").classList.add("hidden");
}
function editStudent(key) {
  const s = students.find(item=>item.key===key);
  if (!s) return;
  $("student-key").value=s.key; $("student-id").value=s.id; $("student-name").value=s.name;
  $("student-email").value=s.email; $("student-phone").value=s.phone||""; $("student-dept").value=s.dept;
  $("student-year").value=s.year; $("student-cgpa").value=s.cgpa||""; $("student-status").value=s.status;
  $("form-title").textContent="Edit student details"; $("save-student").textContent="Update student";
  $("form-error").classList.add("hidden"); showView("add");
}
function showToast(message) {
  $("toast").textContent=message; $("toast").classList.remove("hidden");
  clearTimeout(toastTimer); toastTimer=setTimeout(()=>$("toast").classList.add("hidden"),2600);
}
function handleTableAction(event) {
  const editButton=event.target.closest("[data-edit]");
  const deleteButton=event.target.closest("[data-delete]");
  if (editButton) editStudent(editButton.dataset.edit);
  if (deleteButton) {
    const s=students.find(item=>item.key===deleteButton.dataset.delete);
    if (s && confirm(`Delete ${s.name}'s record? This cannot be undone.`)) {
      students=students.filter(item=>item.key!==s.key); saveStudents(); render(); showToast("Student record deleted.");
    }
  }
}
document.querySelectorAll("[data-view]").forEach(link=>link.addEventListener("click",e=>{e.preventDefault();showView(link.dataset.view);}));
["top-add","welcome-add","list-add"].forEach(id=>$(id).addEventListener("click",goAdd));
$("view-all").addEventListener("click",()=>showView("students"));
$("cancel-form").addEventListener("click",()=>{resetForm();showView("students");});
$("search-input").addEventListener("input",renderStudents);
$("department-filter").addEventListener("change",renderStudents);
$("recent-table").addEventListener("click",handleTableAction);
$("students-table").addEventListener("click",handleTableAction);

$("student-form").addEventListener("submit",event=>{
  event.preventDefault();
  const key=$("student-key").value;
  const record={
    key:key||`student-${Date.now()}`,
    id:$("student-id").value.trim(),
    name:$("student-name").value.trim(),
    email:$("student-email").value.trim(),
    phone:$("student-phone").value.trim(),
    dept:$("student-dept").value,
    year:$("student-year").value,
    cgpa:$("student-cgpa").value,
    status:$("student-status").value
  };
  const error=$("form-error");
  if (record.cgpa!=="" && (Number(record.cgpa)<0 || Number(record.cgpa)>10)) {
    error.textContent="CGPA must be between 0 and 10."; error.classList.remove("hidden"); return;
  }
  const duplicate=students.some(s=>s.id.toLowerCase()===record.id.toLowerCase() && s.key!==key);
  if (duplicate) {
    error.textContent="That student ID already exists. Please use a unique ID."; error.classList.remove("hidden"); return;
  }
  if (key) students=students.map(s=>s.key===key?record:s);
  else students.push(record);
  saveStudents(); render(); resetForm(); showView("students");
  showToast(key?"Student details updated.":"Student registered successfully.");
});
$("today").textContent=new Intl.DateTimeFormat("en-IN",{weekday:"short",day:"numeric",month:"short",year:"numeric"}).format(new Date());
render();
if (location.hash==="#students") showView("students");
else if (location.hash==="#add-student") showView("add");
