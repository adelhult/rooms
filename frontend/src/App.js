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
          seats = {6}
          building = "EDIT"
          equipment = "Whiteboardtavla"
          comments = "Ligger på våning 2 i NC"
          name = "EG-3205"
          startTime = "10:00"
          duration = "2"
      />

      </div>
      </header>
      <h3>Flera lediga rum</h3>
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
