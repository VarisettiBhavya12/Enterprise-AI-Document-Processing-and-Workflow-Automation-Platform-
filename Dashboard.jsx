import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import {
    getDocuments,
    getWorkflows
} from "../services/api";

function Dashboard() {

    const [documents, setDocuments] = useState([]);
    const [workflows, setWorkflows] = useState([]);

    const [loading, setLoading] = useState(true);

    const email = localStorage.getItem("email");

    useEffect(() => {

        loadDashboard();

    }, []);

    const loadDashboard = async () => {

        try {

            const documentsData =
                await getDocuments();

            const workflowsData =
                await getWorkflows();

            setDocuments(documentsData);
            setWorkflows(workflowsData);

        } catch (error) {

            console.error(
                "Dashboard loading error:",
                error
            );

        } finally {

            setLoading(false);
        }
    };

    const pending =
        workflows.filter(
            (item) => item.status === "PENDING"
        ).length;

    const approved =
        workflows.filter(
            (item) => item.status === "APPROVED"
        ).length;

    const rejected =
        workflows.filter(
            (item) => item.status === "REJECTED"
        ).length;

    return (
        <div className="app-page">

            <Navbar />

            <main className="main-container">

                <div className="page-header">

                    <div>
                        <h1>Dashboard</h1>

                        <p>
                            Welcome back, {email}
                        </p>
                    </div>

                    <Link
                        to="/upload"
                        className="primary-button"
                    >
                        + Upload Document
                    </Link>

                </div>

                {loading ? (

                    <div className="loading">
                        Loading dashboard...
                    </div>

                ) : (

                    <>
                        <div className="stats-grid">

                            <div className="stat-card">
                                <div className="stat-icon">
                                    📄
                                </div>

                                <div>
                                    <span>Total Documents</span>
                                    <strong>
                                        {documents.length}
                                    </strong>
                                </div>
                            </div>

                            <div className="stat-card">
                                <div className="stat-icon">
                                    ⏳
                                </div>

                                <div>
                                    <span>Pending</span>
                                    <strong>
                                        {pending}
                                    </strong>
                                </div>
                            </div>

                            <div className="stat-card">
                                <div className="stat-icon">
                                    ✅
                                </div>

                                <div>
                                    <span>Approved</span>
                                    <strong>
                                        {approved}
                                    </strong>
                                </div>
                            </div>

                            <div className="stat-card">
                                <div className="stat-icon">
                                    ❌
                                </div>

                                <div>
                                    <span>Rejected</span>
                                    <strong>
                                        {rejected}
                                    </strong>
                                </div>
                            </div>

                        </div>

                        <div className="dashboard-grid">

                            <div className="content-card">

                                <div className="card-header">
                                    <h2>
                                        Recent Documents
                                    </h2>

                                    <Link to="/documents">
                                        View All
                                    </Link>
                                </div>

                                {documents.length === 0 ? (

                                    <div className="empty-state">
                                        <div>📂</div>
                                        <p>
                                            No documents uploaded yet.
                                        </p>
                                    </div>

                                ) : (

                                    <div className="document-list">

                                        {documents
                                            .slice(-5)
                                            .reverse()
                                            .map((document) => (

                                                <div
                                                    className="document-row"
                                                    key={document.id}
                                                >

                                                    <div className="file-icon">
                                                        📄
                                                    </div>

                                                    <div className="document-info">

                                                        <strong>
                                                            {document.fileName}
                                                        </strong>

                                                        <span>
                                                            {document.fileType || "Document"}
                                                        </span>

                                                    </div>

                                                    <span className="status-badge uploaded">
                                                        {document.status}
                                                    </span>

                                                </div>

                                            ))}

                                    </div>
                                )}

                            </div>

                            <div className="content-card">

                                <div className="card-header">
                                    <h2>
                                        Quick Actions
                                    </h2>
                                </div>

                                <div className="quick-actions">

                                    <Link to="/upload">
                                        <span>⬆️</span>
                                        <div>
                                            <strong>
                                                Upload Document
                                            </strong>
                                            <small>
                                                Add a new document
                                            </small>
                                        </div>
                                    </Link>

                                    <Link to="/documents">
                                        <span>📑</span>
                                        <div>
                                            <strong>
                                                View Documents
                                            </strong>
                                            <small>
                                                Manage your documents
                                            </small>
                                        </div>
                                    </Link>

                                    <Link to="/workflow">
                                        <span>🔄</span>
                                        <div>
                                            <strong>
                                                Manage Workflow
                                            </strong>
                                            <small>
                                                Review pending approvals
                                            </small>
                                        </div>
                                    </Link>

                                </div>

                            </div>

                        </div>
                    </>
                )}

            </main>

        </div>
    );

}

export default Dashboard;