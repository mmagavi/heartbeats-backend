import Slider from "@mui/material/Slider";
import { ReactComponent } from "react-hotkeys";
import {
  Slider_Role,
  Slider_AriaLabel,
} from "../accessibility/Aria";
import { SliderValueLabelProps } from "@mui/material/Slider";
import Tooltip from "@mui/material/Tooltip";
import { Mark } from "@mui/base";

/**
 * Props for SliderUI: currentVal and setCurrentVal of slider
 */
interface SliderProps {
    currentVal: number;
    setCurrentVal: (data: number) => void;
    id: number;
}

//Q: How do I change the color of the marks?
//A: https://mui.com/components/slider/#custom-mark-labels

/**
 * Value label component for the SliderUI component
 * Shows the current value of the slider
 * @param props - SliderValueLabelProps standard
 * @constructor
 */
function ValueLabelComponent(props: SliderValueLabelProps) {
    const {children, value} = props;

    return (
        <Tooltip enterTouchDelay={0} placement="top" title={value}>
            {children}
        </Tooltip>
    );
}

/**
 * Slider component to select a heart rate in range
 * @param props - SliderProps: currentVal & setCurrentVal
 * @constructor
 */
export default function SliderUI(props: SliderProps): ReactComponent {
    let MIN = -1;
    let MAX = -1;
    let DEFAULT = -1;
    let ROLE = "";
    let MARKS: boolean | Mark[] | undefined = [];

  if (props.id == 2) {
    MIN = 13;
    MAX = 100;
    DEFAULT = 35;
    ROLE = "Slider1"
    MARKS = [
          {
              value: 13,
              label: '13',
          },
          {
              value: 100,
              label: '100',
          }
      ];
  } if (props.id == 3) {
    MIN = 15;
    MAX = 180;
    DEFAULT = 30;
    ROLE = "Slider2"
        MARKS = [
            {
                value: 15,
                label: '15',
            },
            {
                value: 180,
                label: '180',
            }
        ];
  }

  function handleChange(n: number) {
    props.setCurrentVal(n);
  }

  return (
    <div
      className="SliderContainer"
      aria-label={Slider_AriaLabel}
      role={Slider_Role}
      tabIndex={0}
    >
      <Slider
          sx={{
            '& .MuiSlider-thumb': {
                color: 'rgb(255,65,65)',
                backgroundImage: 'linear-gradient(315deg, #fg0600 0%, #ff0000 74%)',
            },
            '& .MuiSlider-track': {
              color: 'rgb(255,26,26)',
            },
            '& .MuiSlider-active': {
              color: "green"
            },
          color: "white",
              mark: {
                  color: "red"
              }
          }}
        valueLabelDisplay="on"
        // slots={{
        //   valueLabel: ValueLabelComponent,
        // }}
        aria-label="custom thumb label"
        defaultValue={DEFAULT}
        role={ROLE}
        onChange={(e, n, _) =>
          typeof n === "number" ? handleChange(n) : handleChange(n[0])
        }
        min={MIN}
        max={MAX}
        // marks={MARKS}
      />
    </div>
  );
}
