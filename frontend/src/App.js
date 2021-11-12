import logo from './logo.svg';
import './styles/App.css';
import Room from "./Room.js";



function App() {
  return (
    <div className="App">
      <header className="App-header">
        <h1>Vilket grupprum borde jag gå till?</h1>
      
      <div className="App-featuredRoom">
      <Room
          large
          name = "EG-3205"
          startTime = "10:00"
          duration = "2"
      />

      </div>
      </header>
      <h4>Flera lediga rum</h4>
      <Room
          name = "EG-2516"
          startTime = "14:00"
          duration = "1"
      />
        <Room
          name = "EG-2516"
          startTime = "14:00"
          duration = "1"
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
