import "./styles/LinkButton.css";

export default function LinkButton(props) {
  return (
    <a
      className="LinkButton"
      href={props.href}
    >
        {props.label ?? "Tryck här"}
    </a>
  );
}