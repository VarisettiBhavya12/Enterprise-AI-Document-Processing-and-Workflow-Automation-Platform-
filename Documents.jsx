import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import { getDocuments } from "../services/api";

function Documents() {

    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        loadDocuments();
    }, []);

    const loadDocuments = async () => {

        try {

            const data = await getDocuments();

            setDocuments(data);

        } catch (error) {

            console.error(error);

            setError("Unable to load documents.");

        } finally {

            setLoading(false);
        }
    };

    const openPdf = (id) => {

        window.open(
            `http://localhost:8080/api/documents/${id}/file`,
            "_blank"
        );
    };

    return (
        <div className="app-page">

            <Navbar />

            <main className="main-container">

                <div className="page-header">

                    <div>
                        <h1>Documents</h1>

                        <p>
                            View uploaded documents and AI verification results.
                        </p>
                    </div>

                </div>

                {loading && (
                    <div className="content-card">
                        Loading documents...
                    </div>
                )}

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                {!loading && documents.length === 0 && (
                    <div className="content-card">
                        No documents uploaded yet.
                    </div>
                )}

                <div className="documents-grid">

                    {documents.map((document) => (

                        <div
                            className="content-card document-card"
                            key={document.id}
                        >

                            <div className="document-header">

                                <div>
                                    <h2>
                                        📄 {document.fileName}
                                    </h2>

                                    <p>
                                        Uploaded by:{" "}
                                        {document.uploadedBy}
                                    </p>
                                </div>

                                <span className="status-badge">
                                    {document.status}
                                </span>

                            </div>

                            <div className="document-details">

                                <p>
                                    <strong>Document ID:</strong>{" "}
                                    {document.id}
                                </p>

                                <p>
                                    <strong>AI Status:</strong>{" "}
                                    {document.aiStatus || "Not analyzed"}
                                </p>

                            </div>

                            {document.aiAnalysis && (

                                <div className="ai-result-card">

                                    <h3>
                                        🤖 AI Verification
                                    </h3>

                                    <pre className="ai-analysis">
                                        {document.aiAnalysis}
                                    </pre>

                                </div>

                            )}

                            <div className="document-actions">

                                <button
                                    className="primary-button"
                                    onClick={() =>
                                        openPdf(document.id)
                                    }
                                >
                                    📄 Open PDF
                                </button>

                            </div>

                        </div>

                    ))}

                </div>

            </main>

        </div>
    );
}

export default Documents;