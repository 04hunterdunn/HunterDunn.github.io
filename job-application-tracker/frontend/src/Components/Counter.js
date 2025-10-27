import React, { useState } from 'react';

function Counter() {
  // Declare a state variable called "count" with a setter function "setCount"
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={() => setCount(count + 1)}>Increase</button>
    </div>
  );
}

export default Counter;