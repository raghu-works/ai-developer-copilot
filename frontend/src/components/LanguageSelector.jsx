function LanguageSelector({ language, setLanguage }) {

    return (
        <div className="form-group">

            <label htmlFor="language">
                Language
            </label>

            <select
                id="language"
                value={language}
                onChange={(e) => setLanguage(e.target.value)}
            >
                <option value="Java">Java</option>
                <option value="JavaScript">JavaScript</option>
                <option value="Python">Python</option>
                <option value="C++">C++</option>
                <option value="C">C</option>
                <option value="SQL">SQL</option>
            </select>

        </div>
    );
}

export default LanguageSelector;