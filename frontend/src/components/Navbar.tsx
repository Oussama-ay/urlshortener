function Navbar() {
  return (
    <nav>
      <img src="/images/logo.svg" alt="Shortly" />

      <div>
        <a href="#">Features</a>
        <a href="#">Pricing</a>
        <a href="#">Resources</a>
      </div>

      <div>
        <button>Login</button>
        <button>Sign Up</button>
      </div>
    </nav>
  );
}

export default Navbar;