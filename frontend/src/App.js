import React, { useState, useEffect } from 'react';
import './styles/App.css';
import Room from "./Room.js";
import { getSuggestions } from './api';
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome'
import { faFilter } from '@fortawesome/free-solid-svg-icons'

function Suggestions(props) {
  const [rooms, setRooms] = useState([]);

  useEffect(() => {
    getSuggestions({
      from: props.date.getTime(),
      equipment: props.whiteboard ? "Whiteboard" : ""
    })
      .then(rooms => setRooms(rooms))
      .catch(error => console.error(error));
  }, [props]);

  return rooms.length > 0 ? <>
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

    <h4>Fler förslag på rum</h4>
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
  </> : '';
}


function App() {
  const [showSettings, setShowSettings] = useState(false);
  const [whiteboard, setWhiteboard] = useState(false);

  const today = new Date();
  const [date, setDate] = useState(today.toISOString().split('T')[0]);
  
  const [hours, setHours] = 
    useState(today.getHours() < 10 ?
      '0' + today.getHours() :
      today.getHours()
    );

  const [minutes, setMinutes] = 
    useState(today.getMinutes() < 10 ?
      '0' + today.getMinutes() :
      today.getMinutes()
    );

  const getDate = () => {
    const newDate = new Date(date);
    newDate.setHours(hours);
    newDate.setMinutes(minutes);
    return newDate;
  };

  return (
    <div className="App">
        <section className="App-options">
          <span 
            className="App-showSettings-text"
            onClick={() => setShowSettings(!showSettings)}
          >
            <FontAwesomeIcon icon={faFilter} style={{marginRight:"0.5rem"}}/>
          {
            showSettings ? "Dölj alternativ" : "Visa alternativ"
          }
          </span>
          {
          showSettings &&
            <div className="App-options-more">
              <input type="date" id="start" name="trip-start"
                value={date}
                min={today.toLocaleDateString()}
                onChange={e => setDate(e.target.value)}/>
              <input 
                type="time"
                value={hours + ":" + minutes}
                onChange={e => {
                  const [h, m] = e.target.value.split(':');
                  setHours(h);
                  setMinutes(m);
               }}/>
              <br/>
               <span className="App-whiteboardToggle" onClick={() => setWhiteboard(!whiteboard)}>
                 Endast rum med whiteboard: {whiteboard ? "Ja" : "Nej"}
               </span>
            </div>
          }
        </section>
        <Suggestions
          date={getDate()} 
          whiteboard={whiteboard}
        />
    </div>
    
  );
}

export default App;
