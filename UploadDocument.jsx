import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";

import {
    uploadDocument,
    createWorkflow
} from "../services/api";

function UploadDocument() {

    const navigate = useNavigate();

    const email = localStorage.getItem("email");

    const [file, setFile] = useState(null);
    const [assignedTo, setAssignedTo] = useState("");

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const [loading, setLoading] = useState(false);


    // ==============================
    // FILE SELECTION
    // ==============================

    const handleFileChange = (event) => {

        const selectedFile = event.target.files[0];

        setFile(selectedFile || null);

        setMessage("");
        setError("");
    };


    // ==============================
    // UPLOAD DOCUMENT
    // ==============================

    const handleSubmit = async (event) => {

        event.preventDefault();

        setMessage("");
        setError("");


        // Check file

        if (!file) {

            setError(
                "Please select a document."
            );

            return;
        }


        // Check logged-in user

        if (!email) {

            setError(
                "User email not found. Please login again."
            );

            return;
        }


        setLoading(true);


        try {

            console.log(
                "Starting document upload..."
            );

            console.log(
                "File:",
                file.name
            );

            console.log(
                "User:",
                email
            );


            // ==========================================
            // STEP 1
            // UPLOAD DOCUMENT TO SPRING BOOT
            // ==========================================

            const document = await uploadDocument(
                file,
                email
            );


            console.log(
                "Document upload response:",
                document
            );


            // Check whether backend returned document

            if (!document) {

                throw new Error(
                    "Backend did not return the uploaded document."
                );
            }


            let workflowMessage =
                "Document uploaded successfully.";


            // ==========================================
            // STEP 2
            // CREATE WORKFLOW
            // ==========================================

            if (assignedTo.trim()) {

                try {

                    console.log(
                        "Creating workflow..."
                    );

                    console.log(
                        "Document ID:",
                        document.id
                    );

                    console.log(
                        "Assigned To:",
                        assignedTo
                    );


                    await createWorkflow(
                        document.id,
                        assignedTo.trim()
                    );


                    console.log(
                        "Workflow created successfully."
                    );


                    workflowMessage +=
                        " Workflow created successfully.";


                } catch (workflowError) {

                    console.error(
                        "Workflow creation failed:",
                        workflowError
                    );


                    // Document was already uploaded.
                    // Only workflow failed.

                    workflowMessage +=
                        " However, workflow creation failed.";
                }
            }


            // ==========================================
            // SUCCESS MESSAGE
            // ==========================================

            setMessage(
                workflowMessage
            );


            // ==========================================
            // CLEAR FILE
            // ==========================================

            setFile(null);


            const fileInput =
                window.document.getElementById(
                    "file-input"
                );


            if (fileInput) {

                fileInput.value = "";
            }


            // Clear reviewer

            setAssignedTo("");


        } catch (error) {

            console.error(
                "Document upload error:",
                error
            );


            // ==========================================
            // BACKEND RETURNED AN ERROR
            // ==========================================

            if (error.response) {

                console.error(
                    "Backend response:",
                    error.response
                );


                console.error(
                    "Status:",
                    error.response.status
                );


                console.error(
                    "Response data:",
                    error.response.data
                );


                if (
                    typeof error.response.data ===
                    "string"
                ) {

                    setError(
                        error.response.data
                    );

                } else if (
                    error.response.data?.message
                ) {

                    setError(
                        error.response.data.message
                    );

                } else {

                    setError(
                        `Document upload failed. Server returned status ${error.response.status}.`
                    );
                }


                // ==========================================
                // REQUEST WAS SENT BUT NO RESPONSE
                // ==========================================

            } else if (error.request) {

                console.error(
                    "No response received from backend:",
                    error.request
                );


                setError(
                    "Backend did not respond. Please make sure Spring Boot is running on http://localhost:8080."
                );


                // ==========================================
                // REQUEST SETUP ERROR
                // ==========================================

            } else {

                console.error(
                    "Request error:",
                    error.message
                );


                setError(
                    "Document upload failed: " +
                    error.message
                );
            }


        } finally {

            setLoading(false);
        }
    };


    // ==============================
    // UI
    // ==============================

    return (

        <div className="app-page">

            <Navbar />


            <main className="main-container">


                {/* ==============================
                    PAGE HEADER
                =============================== */}

                <div className="page-header">

                    <div>

                        <h1>
                            Upload Document
                        </h1>

                        <p>
                            Upload a document and optionally
                            assign it for workflow review.
                        </p>

                    </div>

                </div>



                <div className="upload-layout">


                    {/* =================================
                        UPLOAD FORM
                    ================================== */}

                    <div className="content-card upload-card">


                        <form
                            onSubmit={handleSubmit}
                        >


                            {/* File label */}

                            <label>
                                Select Document
                            </label>


                            {/* File upload box */}

                            <div className="file-upload-box">


                                <div className="upload-icon">
                                    ☁️
                                </div>


                                <h3>
                                    Choose a document
                                </h3>


                                <p>
                                    PDF, DOC, DOCX or other
                                    supported files
                                </p>


                                <input
                                    id="file-input"
                                    type="file"
                                    onChange={handleFileChange}
                                />


                                {/* Selected file */}

                                {file && (

                                    <div className="selected-file">

                                        📄 {file.name}

                                    </div>

                                )}

                            </div>



                            {/* =================================
                                WORKFLOW ASSIGNMENT
                            ================================== */}

                            <label>
                                Assign Workflow To
                            </label>


                            <input
                                type="email"
                                placeholder="reviewer@example.com"
                                value={assignedTo}
                                onChange={(event) =>
                                    setAssignedTo(
                                        event.target.value
                                    )
                                }
                            />


                            <p className="field-help">

                                Leave blank if you don't want
                                to create a workflow now.

                            </p>



                            {/* =================================
                                SUCCESS MESSAGE
                            ================================== */}

                            {message && (

                                <div className="success-message">

                                    {message}

                                </div>

                            )}



                            {/* =================================
                                ERROR MESSAGE
                            ================================== */}

                            {error && (

                                <div className="error-message">

                                    {error}

                                </div>

                            )}



                            {/* =================================
                                BUTTONS
                            ================================== */}

                            <div className="form-actions">


                                <button
                                    type="button"
                                    className="secondary-button"
                                    onClick={() =>
                                        navigate("/dashboard")
                                    }
                                    disabled={loading}
                                >

                                    Cancel

                                </button>



                                <button
                                    type="submit"
                                    className="primary-button"
                                    disabled={loading}
                                >

                                    {loading
                                        ? "Uploading..."
                                        : "Upload Document"
                                    }

                                </button>


                            </div>


                        </form>


                    </div>



                    {/* =================================
                        PROCESSING FLOW
                    ================================== */}

                    <div className="content-card information-card">


                        <h2>
                            Processing Flow
                        </h2>



                        {/* STEP 1 */}

                        <div className="process-step">

                            <span>
                                1
                            </span>


                            <div>

                                <strong>
                                    Upload
                                </strong>


                                <p>
                                    Document is stored securely.
                                </p>

                            </div>

                        </div>



                        <div className="process-line"></div>



                        {/* STEP 2 */}

                        <div className="process-step">

                            <span>
                                2
                            </span>


                            <div>

                                <strong>
                                    Process
                                </strong>


                                <p>
                                    Document text is extracted
                                    and analyzed using AI.
                                </p>

                            </div>

                        </div>



                        <div className="process-line"></div>



                        {/* STEP 3 */}

                        <div className="process-step">

                            <span>
                                3
                            </span>


                            <div>

                                <strong>
                                    Workflow
                                </strong>


                                <p>
                                    Assign the document for review.
                                </p>

                            </div>

                        </div>



                        <div className="process-line"></div>



                        {/* STEP 4 */}

                        <div className="process-step">

                            <span>
                                4
                            </span>


                            <div>

                                <strong>
                                    Approval
                                </strong>


                                <p>
                                    Reviewer can approve or reject.
                                </p>

                            </div>

                        </div>


                    </div>


                </div>


            </main>

        </div>
    );
}


export default UploadDocument;