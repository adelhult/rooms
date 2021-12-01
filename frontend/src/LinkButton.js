import "./styles/LinkButton.css";

export default function LinkButton(props) {
  return (
    <a
      className={props.large ? "LinkButton large" : "LinkButton"}
      target="_blank"
      href={props.href}
    >
        {props.label ?? "Tryck här"}
    </a>
  );
}