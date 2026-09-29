import { useEffect, useState } from "react";
import Navbar from "../components/Navbar";
import {
    getWorkflows,
    approveWorkflow,
    rejectWorkflow
} from "../services/api";

function Workflow() {

    const [workflows, setWorkflows] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");

    const loadWorkflows = async () => {

        try {

            const data =
                await getWorkflows();

            setWorkflows(data);

        } catch (error) {

            console.error(error);

            setError(
                "Unable to load workflows."
            );

        } finally {

            setLoading(false);
        }
    };

    useEffect(() => {

        loadWorkflows();

    }, []);

    const handleApprove = async (id) => {

        const comments =
            window.prompt(
                "Enter approval comments:"
            );

        if (comments === null) {
            return;
        }

        try {

            await approveWorkflow(
                id,
                comments
            );

            loadWorkflows();

        } catch (error) {

            console.error(error);

            alert(
                "Unable to approve workflow."
            );
        }
    };

    const handleReject = async (id) => {

        const comments =
            window.prompt(
                "Enter rejection comments:"
            );

        if (comments === null) {
            return;
        }

        try {

            await rejectWorkflow(
                id,
                comments
            );

            loadWorkflows();

        } catch (error) {

            console.error(error);

            alert(
                "Unable to reject workflow."
            );
        }
    };

    return (
        <div className="app-page">

            <Navbar />

            <main className="main-container">

                <div className="page-header">

                    <div>
                        <h1>Workflow Management</h1>

                        <p>
                            Review and manage document workflows
                        </p>
                    </div>

                </div>

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <div className="content-card">

                    {loading ? (

                        <div className="loading">
                            Loading workflows...
                        </div>

                    ) : workflows.length === 0 ? (

                        <div className="empty-state large">

                            <div>🔄</div>

                            <h3>
                                No Workflows Found
                            </h3>

                            <p>
                                Create a workflow by assigning
                                an uploaded document for review.
                            </p>

                        </div>

                    ) : (

                        <div className="table-wrapper">

                            <table>

                                <thead>

                                <tr>
                                    <th>ID</th>
                                    <th>Document ID</th>
                                    <th>Assigned To</th>
                                    <th>Status</th>
                                    <th>Comments</th>
                                    <th>Created</th>
                                    <th>Actions</th>
                                </tr>

                                </thead>

                                <tbody>

                                {workflows.map(
                                    (workflow) => (

                                        <tr key={workflow.id}>

                                            <td>
                                                #{workflow.id}
                                            </td>

                                            <td>
                                                #{workflow.documentId}
                                            </td>

                                            <td>
                                                {workflow.assignedTo}
                                            </td>

                                            <td>

                                                    <span
                                                        className={`status-badge ${workflow.status.toLowerCase()}`}
                                                    >
                                                        {workflow.status}
                                                    </span>

                                            </td>

                                            <td>
                                                {workflow.comments ||
                                                    "-"}
                                            </td>

                                            <td>
                                                {workflow.createdAt
                                                    ? new Date(
                                                        workflow.createdAt
                                                    ).toLocaleString()
                                                    : "-"}
                                            </td>

                                            <td>

                                                {workflow.status ===
                                                "PENDING" ? (

                                                    <div className="action-buttons">

                                                        <button
                                                            className="approve-button"
                                                            onClick={() =>
                                                                handleApprove(
                                                                    workflow.id
                                                                )
                                                            }
                                                        >
                                                            ✓ Approve
                                                        </button>

                                                        <button
                                                            className="reject-button"
                                                            onClick={() =>
                                                                handleReject(
                                                                    workflow.id
                                                                )
                                                            }
                                                        >
                                                            ✕ Reject
                                                        </button>

                                                    </div>

                                                ) : (

                                                    <span className="completed-text">
                                                            Completed
                                                        </span>

                                                )}

                                            </td>

                                        </tr>

                                    )
                                )}

                                </tbody>

                            </table>

                        </div>

                    )}

                </div>

            </main>

        </div>
    );
}

export default Workflow;