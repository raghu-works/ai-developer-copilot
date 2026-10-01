function ResponsePanel({ response }) {

    return (
        <div className="response-container">

            <h2>AI Response</h2>

            {response ? (
                <pre>{response}</pre>
            ) : (
                <p className="empty-response">
                    Your AI response will appear here.
                </p>
            )}

        </div>
    );
}

export default ResponsePanel;