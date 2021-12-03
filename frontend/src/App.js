import React, { useState } from 'react';
import Toggle from 'react-toggle'
import './styles/App.css';
import Preview from "./Preview.js";
import Suggestions from './Suggestions';
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome'
import { faFilter } from '@fortawesome/free-solid-svg-icons'


function App() {
  const [showSettings, setShowSettings] = useState(false);
  const [whiteboard, setWhiteboard] = useState(false);
  const [minSeats, setMinSeats] = useState(1);
  const [onlyBookable, setOnlyBookable] = useState(true);

  const today = new Date();
  const [date, setDate] = useState(today.toISOString().split('T')[0]);
  
  const isPreview = window.location.href.includes('#preview');

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
              <label class="section">Datum och tid</label>
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
              <label class="section">Minsta antalet stolar</label>
              
              <input
                type="number"
                min="0" max="1000"
                onChange={e => setMinSeats(e.target.value)}
                value={minSeats}
              />

              <label class="section">Övrigt</label> 
              <label class="toggle">
                <Toggle
                  defaultChecked={whiteboard}
                  className='my-toggle'
                  onChange={() => setWhiteboard(!whiteboard)} />
                <span>Visa endast rum med whiteboard</span>
              </label>
              <br/>
              <label class="toggle">
                <Toggle
                  className='my-toggle'
                  defaultChecked={onlyBookable}
                  onChange={() => setOnlyBookable(!onlyBookable)} />
                <span>Visa enbart bokningsbara rum</span>
              </label>
            </div>
          }
        </section>
        {
          isPreview ?
          <Preview
            date={getDate()} 
            whiteboard={whiteboard}
            minSeats={minSeats}
            onlyBookable={onlyBookable}
          />
          :
          <Suggestions
            date={getDate()} 
            whiteboard={whiteboard}
            minSeats={minSeats}
            onlyBookable={onlyBookable}
          />
        }

    </div>
    
  );
}

export default App;
