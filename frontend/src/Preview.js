import React, { useState, useEffect } from 'react';
import Room from './Room';
import { getSuggestions } from './api';
import LinkButton from './LinkButton';
import "./styles/Preview.css";

export default function Preview(props) {
  const [rooms, setRooms] = useState([]);

  useEffect(() => {
    getSuggestions({
        number: 1,
        from: props.date.getTime(),
        equipment: props.whiteboard ? "Whiteboard" : "",
        minSeats: props.minSeats,
        onlyBookable: props.onlyBookable,
    })
      .then(rooms => setRooms(rooms))
      .catch(error => console.error(error));
  }, [props]);

  const roomElements = rooms.map((room, index) => (
      <Room
        {...room}
        large={index === 0}
        key={room.name}
      />
  ));

  return roomElements.length > 0 ?
    <header className="Preview">
        <h1>Grupprumsförslag</h1>
        <div className="featuredRoom">
            {roomElements[0]}
        </div>
        <div class="interactions">
            <a href="." target="_blank">Visa fler förslag</a>
        </div>
    </header>
  : 
  <h1>
    <span class="delayedDisplay">Hittade inga rum :(</span>
  </h1>;
}
