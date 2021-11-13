import React, { useState, useEffect } from 'react';
import './styles/App.css';
import Room from "./Room.js";
import { getSuggestions } from './api';

function App() {
  const [rooms, setRooms] = useState([]);
  
  useEffect(() => {
    getSuggestions()
      .then(rooms => setRooms(rooms))
      .catch(error => console.error(error));
  }, []);

  return (
    <div className="App">
      
      {rooms.length > 0 &&
        <>
          <header className="App-header">
            <h1>Vilket grupprum borde jag gå till?</h1>
            <div className="App-featuredRoom">
              <Room
                  large
                  name={rooms[0].name}
                  startTime={rooms[0].startTime}
                  duration={rooms[0].duration}
              />
            </div>
          </header>
          <h4>Flera lediga rum</h4>
          {rooms.slice(1).map(room => (
            <Room
              name={room.name}
              startTime={room.startTime}
              duration={room.duration}
            />
          ))}
        </>
      }
    </div>
  );
}

export default App;
