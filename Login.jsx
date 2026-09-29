import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../services/api";

function Login() {
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleLogin = async (event) => {
        event.preventDefault();

        setError("");
        setLoading(true);

        try {
            const result = await loginUser(email, password);

            console.log("Login response:", result);

            const text = String(result);

            if (text.startsWith("Login successful")) {
                const tokenPart = text.split("JWT Token:")[1];

                if (!tokenPart) {
                    setError("JWT token was not received from the backend.");
                    return;
                }

                const token = tokenPart.trim();

                localStorage.setItem("token", token);
                localStorage.setItem("email", email);

                navigate("/dashboard");
            } else {
                setError(text);
            }

        } catch (error) {
            console.error("LOGIN ERROR:", error);

            if (error.response) {
                console.error("Status:", error.response.status);
                console.error("Response:", error.response.data);

                const responseData = error.response.data;

                if (typeof responseData === "string") {
                    setError(responseData);
                } else if (responseData?.message) {
                    setError(responseData.message);
                } else {
                    setError(
                        `Login failed. Server returned ${error.response.status}.`
                    );
                }

            } else if (error.request) {
                console.error(
                    "Request was sent but no response was received:",
                    error.request
                );

                setError(
                    "The backend did not respond. Check that Spring Boot is running on port 8080."
                );

            } else {
                console.error("Request setup error:", error.message);

                setError(
                    `Request error: ${error.message}`
                );
            }

        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-page">

            <div className="login-left">

                <div className="login-brand">

                    <div className="big-logo">
                        📄
                    </div>

                    <h1>DocFlow AI</h1>

                    <p>
                        Enterprise AI Document Processing
                        & Workflow Automation Platform
                    </p>

                </div>

                <div className="feature-list">

                    <div>
                        <span>📁</span>

                        <div>
                            <strong>Smart Documents</strong>

                            <p>
                                Upload and manage business documents.
                            </p>
                        </div>
                    </div>

                    <div>
                        <span>🔍</span>

                        <div>
                            <strong>Document Processing</strong>

                            <p>
                                Process and extract information from documents.
                            </p>
                        </div>
                    </div>

                    <div>
                        <span>🔄</span>

                        <div>
                            <strong>Automated Workflow</strong>

                            <p>
                                Manage approval and review processes.
                            </p>
                        </div>
                    </div>

                </div>

            </div>

            <div className="login-right">

                <div className="login-card">

                    <h2>Welcome Back</h2>

                    <p className="login-subtitle">
                        Sign in to your document workspace
                    </p>

                    {error && (
                        <div className="error-message">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleLogin}>

                        <label>Email Address</label>

                        <input
                            type="email"
                            placeholder="Enter your email"
                            value={email}
                            onChange={(e) =>
                                setEmail(e.target.value)
                            }
                            required
                        />

                        <label>Password</label>

                        <input
                            type="password"
                            placeholder="Enter your password"
                            value={password}
                            onChange={(e) =>
                                setPassword(e.target.value)
                            }
                            required
                        />

                        <button
                            className="primary-button login-button"
                            type="submit"
                            disabled={loading}
                        >
                            {loading
                                ? "Signing in..."
                                : "Sign In"}
                        </button>

                    </form>

                    <div className="demo-info">
                        <strong>Demo Login</strong>

                        <br />

                        Use a user already registered in your
                        Spring Boot database.
                    </div>

                </div>

            </div>

        </div>
    );
}

export default Login;
