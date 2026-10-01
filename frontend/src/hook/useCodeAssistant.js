import { useState } from "react";
import { sendCode } from "../services/api";

function useCodeAssistant() {

    const [response, setResponse] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const executeCode = async ({
        code,
        language,
        operation,
        sessionId
    }) => {

        if (!code.trim()) {
            setError("Please enter some code.");
            return;
        }

        try {

            setLoading(true);
            setError("");
            setResponse("");

            const data = await sendCode({
                code,
                language,
                operation,
                sessionId
            });

            setResponse(data);

        } catch (err) {

            console.error(err);

            setError(
                "Unable to connect to the backend."
            );

        } finally {

            setLoading(false);
        }
    };

    return {
        response,
        loading,
        error,
        executeCode
    };
}

export default useCodeAssistant;