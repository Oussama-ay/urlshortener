import { Link } from "react-router-dom";

function Navbar() {
  return (
    <nav>
      <Link to="/" aria-label="Shortly home">
        <img src="/images/logo.svg" alt="Shortly" />
      </Link>

      <div>
        <a href="#">Features</a>
        <a href="#">Pricing</a>
        <a href="#">Resources</a>
        <Link to="/my-links">My Links</Link>
      </div>

      <div>
        <Link className="nav-action" to="/login">Login</Link>
        <Link className="nav-action nav-signup" to="/register">Sign Up</Link>
      </div>
    </nav>
  );
}

export default Navbar;
