import "./styles/LinkButton.css";

export default function LinkButton(props) {
  return (
    <a
      className="LinkButton"
      target="_blank"
      href={props.href}
    >
        {props.label ?? "Tryck här"}
    </a>
  );
}