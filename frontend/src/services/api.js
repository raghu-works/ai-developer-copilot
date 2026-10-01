import axios from "axios";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

export const sendCode = async (codeData) => {
    const response = await api.post("/code", codeData);
    return response.data;
};

export const checkBackend = async () => {
    const response = await api.get("/hello");
    return response.data;
};

export default api;