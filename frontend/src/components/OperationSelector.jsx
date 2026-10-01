function OperationSelector({ operation, setOperation }) {

    return (
        <div className="form-group">

            <label htmlFor="operation">
                What do you want AI to do?
            </label>

            <select
                id="operation"
                value={operation}
                onChange={(e) => setOperation(e.target.value)}
            >
                <option value="EXPLAIN">
                    Explain Code
                </option>

                <option value="DEBUG">
                    Debug Code
                </option>

                <option value="OPTIMIZE">
                    Optimize Code
                </option>

                <option value="GENERATE">
                    Generate Code
                </option>

                <option value="TEST">
                    Generate Tests
                </option>

                <option value="DOCUMENT">
                    Generate Documentation
                </option>
                   <option value="SQL">
                        Generate SQL
                         </option>

            </select>

        </div>
    );
}

export default OperationSelector;