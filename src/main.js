

function Register_page() {
  window.location.href = 'register_page.html'; // 跳轉到Register頁面
}

function Login_page() {
  window.location.href = 'login_page.html'; // 跳轉到Login頁面
}

function Home_page() {
  window.location.href = 'Main page.html'; // 跳轉到Login頁面
}

function format_check(){
  let Email = document.getElementById('u13_input').value;
  let Address = document.getElementById('u12_input').value;
  let Company = document.getElementById('u15_input').value;
  let Password = document.getElementById('u17_input').value;

  if (Email.includes('@')) {
    document.getElementById('myForm').submit();
  }else if (Email.includes(' ') || Address.includes(' ') || Company.includes(' ') || Password.includes(' ')) {
    window.location.href = 'register_page.html';
    document.getElementById('error').innerHTML = "Something is going wrong... please try again.";
  }
}

