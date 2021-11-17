import React, { useState, useEffect } from 'react';
import Room from './Room';
import { getSuggestions } from './api';

function Suggestions(props) {
  const [rooms, setRooms] = useState([]);

  useEffect(() => {
    getSuggestions({
      from: props.date.getTime(),
      equipment: props.whiteboard ? "Whiteboard" : "",
      minSeats: props.minSeats,
    })
      .then(rooms => setRooms(rooms))
      .catch(error => console.error(error));
  }, [props]);

  const roomElements = rooms.map((room, index) => (
      <Room
        {...room}
        large={index == 0}
        key={room.name}
      />
  ));

  return roomElements.length > 0 ? <>
    <header className="App-header">
      <h1>Ledigt rum</h1>
      <div className="App-featuredRoom">
        {roomElements[0]}
      </div>
    </header>

    <h4>Fler förslag på rum</h4>
      {
        roomElements.length < 2 ?
          <p>Det finns inga fler förslag på rum.</p> :
          roomElements.slice(1)
      }
  </> : 
  <h1>
    <span class="delayedDisplay">Hittade inga rum :(</span>
  </h1>;
}

export default Suggestions;
