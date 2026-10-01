function CodeEditor({ code, setCode }) {

    return (
        <div className="editor-container">

            <label htmlFor="code">
                Your Code
            </label>

            <textarea
                id="code"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                placeholder="// Paste your code here..."
                spellCheck="false"
            />

        </div>
    );
}

export default CodeEditor;