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
                <strong>Antal platser: </strong> {props.seatcount ?? "okänt"}
            </li>
            <li>
                <FontAwesomeIcon className="Room-inlineIcon" icon={faBuilding}/>
                <strong>Byggnad: </strong>
                <p>{props.building ?? "okänt"}</p>
            </li>
            {props.equipment &&
            <li>
                <FontAwesomeIcon className="Room-inlineIcon" icon={faChalkboard}/>
                <strong>Utrustning: </strong>
                <p>{props.equipment ?? "okänt"}</p>
            </li>
            }
            {props.comment &&
            <li>
                <FontAwesomeIcon className="Room-inlineIcon" icon={faCommentAlt}/>
                <strong>Övrigt: </strong>
                <p>{props.comment ?? "okänt"}</p>
            </li>
            }
            <li>
                <FontAwesomeIcon className="Room-inlineIcon" icon={faMapMarkedAlt}/>
                <a target="_blank"
                   href={`https://www.google.com/maps/place/${props.latitude},${props.longitude}`}><strong>Länk till
                    karta</strong> </a>
            </li>
            <li>
                <FontAwesomeIcon className="Room-inlineIcon" icon={faStreetView}/>
                <a target="_blank" href={props.chalmersMapsLink}><strong>Länk till Chalmers maps</strong> </a>
            </li>
        </ul>
    }

    return <div className="Room" key={props.name}>
        <header onClick={() => setDisplayInfo(!displayInfo)}>
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
                if (props.timeslot.start - new Date().getTime() < 100000) {
                    return "nu"
                } else {
                    return "från " + new Date(props.timeslot.start).toLocaleTimeString('sv-SE', {
                        hour: '2-digit',
                        minute: "2-digit"
                    });
                }
            })()}
            &nbsp;
            {
                (() => {
                    const lastMin = new Date(props.timeslot.start);
                    lastMin.setHours(23);
                    lastMin.setMinutes(59);

                    if (props.timeslot.end > lastMin.getTime()) {
                        if (new Date(props.timeslot.start).getHours() >= 17) {
                            return "för resten av kvällen."
                        }
                        return "för resten av dagen."
                    } else {
                        let hours =  Math.floor(props.duration / 3600000);
                        let minutes = Math.floor((props.duration % 3600000) / 60000);
                        
                        if (hours !== 0 && minutes !== 0) {
                            return "i " + hours + (hours === 1 ? " timme" : " timmar") + " och " +
                            + minutes + (minutes === 1 ? " minut" : " minuter") + ".";
                        }
                        if (hours !== 0 && minutes === 0) {
                            return "i " + hours + (hours === 1 ? " timme" : " timmar") + ".";
                        }
                        if (hours === 0 && minutes !== 0) {
                            return "i " + minutes + (minutes === 1 ? " minut" : " minuter") + ".";
                        }
                        
                        return "";
                    }
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

