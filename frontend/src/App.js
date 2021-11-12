import logo from './logo.svg';
import './styles/App.css';
import Room from "./Room.js";



function App() {
  return (
    <div className="App">
      <header className="App-header">
      </header>
      <Room
          name = "EG-3205"
          startTime = "10:00"
          duration = "2"
      />

      <Room
          name = "EG-2516"
          startTime = "14:00"
          duration = "1"
      />
    </div>
  );
}

export default App;
