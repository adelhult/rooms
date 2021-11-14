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

  return rooms.length > 0 ? ( 
    <div className="App">
        <>
          <header className="App-header">
            <h1>Här är det ledigt</h1>
            <div className="App-featuredRoom">
              <Room
                  large
                  name={rooms[0].name}
                  startTime={rooms[0].timeslot.start}
                  endTime={rooms[0].timeslot.end}
                  duration={rooms[0].duration}
                  building={rooms[0].building}
                  seats={rooms[0].seatcount}
                  comments={rooms[0].comment}
                  equipment={rooms[0].equipment}
                  chalmersMapsLink={rooms[0].chalmersMapsLink}
                  latitude={rooms[0].latitude}
                  longitude={rooms[0].longitude}
              />
            </div>
          </header>
          <h4>Fler lediga rum</h4>
          {rooms.slice(1).map(room => (
            <Room
              name={room.name}
              startTime={room.timeslot.start}
              endTime={room.timeslot.end}
              duration={room.duration}
              building={room.building}
              seats={room.seatcount}
              comments={room.comment}
              equipment={room.equipment}
              chalmersMapsLink={room.chalmersMapsLink}
              latitude={room.latitude}
              longitude={room.longitude}
            />
          ))}
        </>
    </div>
    
  ) : '';
}

export default App;
