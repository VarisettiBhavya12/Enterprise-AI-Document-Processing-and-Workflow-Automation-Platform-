import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api";

const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 60000,
});


// ======================================================
// JWT TOKEN INTERCEPTOR
// ======================================================

api.interceptors.request.use(
    (config) => {

        const token = localStorage.getItem("token");

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },

    (error) => {
        return Promise.reject(error);
    }
);


// ======================================================
// LOGIN
// ======================================================

export const loginUser = async (email, password) => {

    const response = await api.post(
        "/auth/login",
        {
            email,
            password,
        }
    );

    return response.data;
};


// ======================================================
// GET ALL DOCUMENTS
// ======================================================

export const getDocuments = async () => {

    const response = await api.get(
        "/documents"
    );

    return response.data;
};


// ======================================================
// GET USER DOCUMENTS
// ======================================================

export const getUserDocuments = async (email) => {

    const response = await api.get(
        `/documents/user/${encodeURIComponent(email)}`
    );

    return response.data;
};


// ======================================================
// GET ONE DOCUMENT
// ======================================================

export const getDocument = async (id) => {

    const response = await api.get(
        `/documents/${id}`
    );

    return response.data;
};


// ======================================================
// UPLOAD DOCUMENT
// ======================================================

export const uploadDocument = async (file, email) => {

    if (!file) {
        throw new Error(
            "No file selected."
        );
    }

    if (!email) {
        throw new Error(
            "User email is missing."
        );
    }


    const formData = new FormData();

    formData.append(
        "file",
        file
    );

    formData.append(
        "email",
        email
    );


    console.log(
        "Uploading document:",
        file.name
    );

    console.log(
        "Upload user:",
        email
    );


    try {

        const response = await api.post(
            "/documents/upload",
            formData,
            {
                headers: {
                    "Content-Type": "multipart/form-data",
                },

                timeout: 120000,
            }
        );


        console.log(
            "Upload response:",
            response.data
        );


        return response.data;

    } catch (error) {

        console.error(
            "Document upload API error:",
            error
        );


        if (error.response) {

            console.error(
                "Backend status:",
                error.response.status
            );

            console.error(
                "Backend response:",
                error.response.data
            );
        }


        throw error;
    }
};


// ======================================================
// OPEN DOCUMENT FILE
// ======================================================

export const getDocumentFileUrl = (id) => {

    return `${API_BASE_URL}/documents/${id}/file`;
};


// ======================================================
// DELETE DOCUMENT
// ======================================================

export const deleteDocument = async (id) => {

    const response = await api.delete(
        `/documents/${id}`
    );

    return response.data;
};


// ======================================================
// CREATE WORKFLOW
// ======================================================

export const createWorkflow = async (
    documentId,
    assignedTo
) => {

    const response = await api.post(
        "/workflows",
        {
            documentId,
            assignedTo,
        }
    );

    return response.data;
};


// ======================================================
// GET WORKFLOWS
// ======================================================

export const getWorkflows = async () => {

    const response = await api.get(
        "/workflows"
    );

    return response.data;
};


// ======================================================
// APPROVE WORKFLOW
// ======================================================

export const approveWorkflow = async (
    workflowId,
    comment
) => {

    const response = await api.put(
        `/workflows/${workflowId}/approve`,
        {
            comment,
        }
    );

    return response.data;
};


// ======================================================
// REJECT WORKFLOW
// ======================================================

export const rejectWorkflow = async (
    workflowId,
    comment
) => {

    const response = await api.put(
        `/workflows/${workflowId}/reject`,
        {
            comment,
        }
    );

    return response.data;
};


export default api;