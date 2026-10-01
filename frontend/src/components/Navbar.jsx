import { Link } from "react-router-dom";

function Navbar() {

    return (
        <nav className="navbar">

            <div className="navbar-container">

                <Link
                    to="/"
                    className="logo"
                >
                    AI Developer Copilot
                </Link>

                <div className="nav-links">

                    <Link to="/">
                        Home
                    </Link>

                    <Link to="/login">
                        Login
                    </Link>

                </div>

            </div>

        </nav>
    );
}

export default Navbar;