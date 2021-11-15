import React, {useState} from 'react';
import "./styles/Rooms.css"
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome'
import {
    faBuilding,
    faChair,
    faChalkboard,
    faCommentAlt,
    faInfoCircle,
    faMapMarkedAlt,
    faStreetView
} from '@fortawesome/free-solid-svg-icons'
import LinkButton from './LinkButton';

/*
    Props
    - name
    - startTime
    - duration
    - expanded - if true, all info will be shown
    - ==== info =====
    - equipment
    - building
    - seats
    - comments
    - latitude
    - longitude
    - chalmerMapsLink
    - large
*/
export default function Room(props) {
    const [displayInfo, setDisplayInfo] = useState(props.expanded);

    const generateInfo = props => {
        return <ul>
            <li>
                <FontAwesomeIcon className="Room-inlineIcon" icon={faChair}/>
                <strong>Antal platser: </strong> {props.seats ?? "okänt"}
            </li>
            <li>
                <FontAwesomeIcon className="Room-inlineIcon" icon={faBuilding}/>
                <strong>Byggnad: </strong>
                <p>{props.building ?? "okänt"}</p>
            </li>
            <li>
                <FontAwesomeIcon className="Room-inlineIcon" icon={faChalkboard}/>
                <strong>Utrustning: </strong>
                <p>{props.equipment ?? "okänt"}</p>
            </li>
            {props.comment &&
            <li>
                <FontAwesomeIcon className="Room-inlineIcon" icon={faCommentAlt}/>
                <strong>Övrigt: </strong>
                <p>{props.comments ?? "okänt"}</p>
            </li>
            }
            <li>
                <FontAwesomeIcon className="Room-inlineIcon" icon={faMapMarkedAlt}/>
                <a target="_blank"
                   href={`https://www.google.se/maps/?q=loc:${props.latitude},${props.longitude}`}><strong>Länk till
                    karta</strong> </a>
            </li>
            <li>
                <FontAwesomeIcon className="Room-inlineIcon" icon={faStreetView}/>
                <a target="_blank" href={props.chalmersMapsLink}><strong>Länk till Chalmers maps</strong> </a>
            </li>
        </ul>
    }

    return <div className="Room" key={props.name} onClick={() => setDisplayInfo(!displayInfo)}>
        <header>
            <h2 className={`Room-name ${props.large ? 'large' : ''}`}>{props.name}</h2>
            <FontAwesomeIcon
                size="lg"
                className="Room-expand"
                icon={faInfoCircle}
            />
        </header>

        <span className="Room-timeinfo">
            Ledigt&nbsp;
            {(() => {
                if (props.startTime - new Date().getTime() < 100000) {
                    return "nu"
                } else {
                    return "från " + new Date(props.startTime).toLocaleTimeString('sv-SE', {
                        hour: '2-digit',
                        minute: "2-digit"
                    });
                }
            })()}
            &nbsp;
            {
                (() => {
                    let lastMin = new Date(props.startTime);
                    lastMin.setHours(23);
                    lastMin.setMinutes(59);

                    if (props.endTime > lastMin.getTime()) {
                        if (new Date(props.startTime).getHours >= 18) {
                            return "för resten av kvällen."
                        }
                        return "för resten av dagen."
                    } else {
                        let hours =  Math.floor(props.duration / 3600000);
                        let minutes = Math.floor((props.duration % 3600000) / 60000);
                        return "i " + 
                        (hours != 0 ? hours + (hours == 1 ? " timme" : " timmar") : "") + 
                        (minutes != 0 ? " och " + minutes + (minutes == 1 ? " minut" : " minuter") : "")    + "."                 }
                })()
            }
        </span>

        {
            displayInfo &&
            <div className="Room-info">
                {generateInfo(props)}
            </div>
        }
        {
            (displayInfo || props.large) &&
            <div className="Room-actions">
                <LinkButton label="Boka grupprummet" href="https://cloud.timeedit.net/chalmers/web/b1"/>
            </div>
        }


    </div>
}

