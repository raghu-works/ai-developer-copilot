import { useState } from "react";

import LanguageSelector from "../components/LanguageSelector";
import OperationSelector from "../components/OperationSelector";
import CodeEditor from "../components/CodeEditor";
import ResponsePanel from "../components/ResponsePanel";
import Loading from "../components/Loading";

import useCodeAssistant from "../hook/useCodeAssistant";

function Home() {

    const [code, setCode] = useState("");

    const [language, setLanguage] =
        useState("Java");

    const [operation, setOperation] =
        useState("EXPLAIN");

    /*
     * For now we use sessionId = 1.
     * Later this will come from the logged-in user/session.
     */
    const sessionId = 1;

    const {
        response,
        loading,
        error,
        executeCode
    } = useCodeAssistant();

    const handleSubmit = async () => {

        await executeCode({
            code,
            language,
            operation,
            sessionId
        });
    };

    return (

        <main className="home">

            <div className="home-container">

                <div className="hero">

                    <h1>
                        AI Developer Copilot
                    </h1>

                    <p>
                        Explain, debug, optimize and
                        generate code using AI.
                    </p>

                </div>

                <div className="control-panel">

                    <LanguageSelector
                        language={language}
                        setLanguage={setLanguage}
                    />

                    <OperationSelector
                        operation={operation}
                        setOperation={setOperation}
                    />

                </div>

                <CodeEditor
                    code={code}
                    setCode={setCode}
                />

                <button
                    className="ask-button"
                    onClick={handleSubmit}
                    disabled={loading}
                >
                    {loading
                        ? "Processing..."
                        : "Ask AI"
                    }
                </button>

                {loading && <Loading />}

                {error && (
                    <div className="error">
                        {error}
                    </div>
                )}

                <ResponsePanel
                    response={response}
                />

            </div>

        </main>
    );
}

export default Home;