

function createAccount() {
  window.location.href = 'createAccount.html'; // 跳轉到Register頁面
}

function Login_page() {
  window.location.href = 'login_page.html'; // 跳轉到Login頁面
}

function Home_page() {
  window.location.href = 'index.html'; // 跳轉到Login頁面
}

function format_check(){
  let Email = document.getElementById('u13_input').textContent;
  let Address = document.getElementById('u12_input').textContent;
  let Company = document.getElementById('u15_input').textContent;
  let Password = document.getElementById('u17_input').textContent;

  if (Email.includes('@')) {
    document.getElementById('myForm').submit();
    window.location.href = 'login_page.html';
  }else if (Email.includes(' ') || Address.includes(' ') || Company.includes(' ') || Password.includes(' ')) {
    window.location.href = 'createAccount.html';
    document.getElementById('error').innerHTML = "Something is going wrong... please try again.";
  }
}

