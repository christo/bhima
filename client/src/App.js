import React, {useEffect, useState} from 'react';
import './App.css';
import {
    Alert,
    AppBar,
    Box,
    Button,
    CircularProgress,
    Container,
    createTheme,
    CssBaseline,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    IconButton,
    Paper,
    Stack,
    ThemeProvider,
    Toolbar
} from "@mui/material";
import {
    AppRegistration,
    Apps,
    Cable,
    ConnectedTv,
    DirectionsBus,
    Image,
    Movie,
    QuestionMark,
    Settings,
    TextFields
} from "@mui/icons-material";
import {BrowserRouter, NavLink, Route, Routes} from "react-router-dom";
import {purple} from "@mui/material/colors";
import Logo from "./dragon-head-neg.png";

// TODO read how to reduce bundle size from imports: https://mui.com/guides/minimizing-bundle-size/

/** Movie program, playing video file at configured speed, loops for duration if shorter than duration. */
const TYPE_MOVIE = "Movie";
/** (planned) Code that produces animation, possibly interactive, possibly sound-responsive. */
const TYPE_ALGORITHM = "Algorithm";
/** Image with configured animation, e.g. kaliedoscope, rotozoom, scroll etc. */
const TYPE_IMAGE = "Image";
/** Video Stream */
const TYPE_STREAM = "Stream";
/** Text scroller */
const TYPE_TEXT = "Text";

// TODO complete colour scheme: purple/aqua/pink/lightgrey/black  (?)
const bhimaTheme = createTheme({
    palette: {
        mode: 'dark',
        primary: {
            // Purple and green play nicely together.
            main: purple[500],
        },
        secondary: {
            // This is green.A700 as hex.
            main: '#11cb5f',
        },
    },
});


function getEndpoint(name) {
    return '//' + window.location.hostname + ":9000/api/bhima/" + name;
}

function getHeaders() {
    return {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };
}

function bhimaFetch(name) {
    // TODO drive from config with devmode / production
    return fetch(getEndpoint(name), {
        headers: getHeaders()
    }).then(res => res.json());
}

/**
 * Shows the icon for the given program type (e.g. Movie, Program, ...future types).
 *
 * @param props
 * @returns {JSX.Element}
 * @constructor
 */
const ProgramTypeIcon = (props) => {
    switch(props.type) {
        case TYPE_MOVIE: return <Movie fontSize="large" className="programType" />;
        case TYPE_ALGORITHM: return <Apps fontSize="large" className="programType" />;
        case TYPE_IMAGE: return <Image fontSize="large" className="programType" />;
        case TYPE_STREAM: return <ConnectedTv fontSize="large" className="programType" />;
        case TYPE_TEXT: return <TextFields fontSize="large" className="programType" />;
        default: return <QuestionMark fontSize="large" className="programType" />;
    }
};

const TopBar = (props) => {
    // TODO highlight current page
    // TODO make topbar slide in/out on scroll
    return <AppBar position="fixed" color="primary" title="Bhima">
        <Toolbar sx={{justifyItems: "center"}}>
            <Box
                component="img"
                sx={{maxHeight: 50, overflow: "hidden", objectFit: "contain", mr: 2}}
                alt="Bhima logo"
                className="logo"
                src={Logo}
            />
            <NavLink to="/" end className={(props) => `${props.isActive ? 'active ' : ''}`} >
                <IconButton
                    size="large"
                    edge="start"
                    color="inherit"
                    aria-label="menu"
                    sx={{mr: 3, color: "text-secondary"}}
                >
                    <DirectionsBus/>
                </IconButton>
            </NavLink>
            <NavLink to="/settings" end className={(props) => `${props.isActive ? 'active ' : ''}`} >
                <IconButton
                    size="large"
                    edge="start"
                    color="inherit"
                    aria-label="menu"
                    sx={{mr: 3}}
                >
                    <Settings/>
                </IconButton>
            </NavLink>

            <NavLink to="/programs" end className={(props) => `${props.isActive ? 'active ' : ''}`} >
                <IconButton
                    size="large"
                    edge="start"
                    color="inherit"
                    aria-label="menu"
                    sx={{mr: 3}}
                >
                    <AppRegistration/>
                </IconButton>
            </NavLink>

            <NavLink to="/wiring" end className={(props) => `${props.isActive ? 'active ' : ''}`} >
                <IconButton
                    size="large"
                    edge="start"
                    color="inherit"
                    aria-label="menu"
                    sx={{mr: 3}}
                >
                    <Cable/>
                </IconButton>
            </NavLink>
        </Toolbar>
    </AppBar>;
};

const CurrentProgram = (props) => {
    const [loaded, setLoaded] = useState(false);
    const [program, setProgram] = useState(null);
    const [error, setError] = useState(null);
    useEffect(() => {
        bhimaFetch("program")
            .then(
                (result) => {
                    setProgram(result);
                    setLoaded(true);
                },
                (error) => {
                    setError(error);
                    setLoaded(true);
                }
            );

    }, []);
    if (error) {
        return <Alert severity="error">Dragonmind link failure: {error.message}</Alert>
    } else if (!loaded) {
        return <CircularProgress color="secondary" />;
    } else {
        if (program === null) {
            return null;
        } else {
            return <ProgramCard program={program}/>;
        }
    }
}


const ProgramCard = (props) => {
    // TODO make clickable to show more info and run button, unshow on click out
    const {program, onRun} = props;

    let [open, setOpen] = React.useState(false);

    const handleOpen = () => {
        setOpen(true);
    }
    const handleCancel = () => {
        setOpen(false);
    }
    const handleRun = () => {
        setOpen(false);
        onRun(program);
    };

    return <React.Fragment>
        <Dialog open={open} onClose={handleCancel}>
            <DialogTitle>{`Run ${program.name} now?`}</DialogTitle>
            <DialogActions>
                <Button onClick={handleCancel}>Cancel</Button>
                <Button onClick={handleRun} autoFocus>Run</Button>
            </DialogActions>
        </Dialog>
        <Paper className="program" onClick={handleOpen}>
            <img className="thumbnail" alt={`thumbnail for ${program.name}`}
                 src={"data:image/jpg;charset=utf-8;base64," + program.thumbnail}/>
            <ProgramTypeIcon type={program.type}/>
            <span className="programName">{program.name}</span>
        </Paper>
    </React.Fragment>;
};

const ProgramList = (props) => {
    const {onRun} = props;
    const [loaded, setLoaded] = useState(false);
    const [programs, setPrograms] = useState([]);
    const [error, setError] = useState(null);
    useEffect(() => {
        bhimaFetch("programs").then(
            (result) => {
                setLoaded(true);
                setPrograms(result);
            },
            (error) => {
                setLoaded(true);
                setError(error);
            }
        );
    }, []);
    if (error) {
        return null;    // TODO hoist error state for page-wide singleton error reporting
    } else if (!loaded) {
        return <CircularProgress color="secondary" />;
    } else {
        return (
            <Stack spacing={2} sx={{margin: "auto"}}>
                {programs.map(program => (
                    <ProgramCard key={program.id} program={program} onRun={onRun}/>
                ))}
            </Stack>
        );
    }
};

const HomePage = (props) => {
    const onRun = (program) => {
        console.log(`running ${program.id}`);

        let data = new FormData();
        data.append("id", program.id);
        fetch(getEndpoint("runProgram2?id=" + encodeURI(program.id)), {
            method: 'POST',
            headers: getHeaders()
        }).then(res => res.json());
        // TODO update current program state

    }
    return (
        <div className="page">
            <h3>Live</h3>
            <CurrentProgram/>
            <h3>All Programs</h3>
            <ProgramList onRun={onRun}/>
        </div>
    );
};


const WiringPage = (props) => {
    return (
    <div className="page">
        <h3>Wiring</h3>
        This is where the wiring page goes, yo.

    </div>);
};

const SettingsPage = (props) => {
    return (<div className="page">
        <h3>Settings</h3>
        This is where the settings page goes, yo.

    </div>);
};

const ProgramsPage = () => {
    return <div className="page">
        <h3>Program Configuration</h3>
        <ul className="programTypes">
            <li><ProgramTypeIcon type="Algorithm"/> Algorithm: code module</li>
            <li><ProgramTypeIcon type="Movie"/> Movie: looping video file</li>
            <li><ProgramTypeIcon type="Image"/> Image: animated image</li>
            <li><ProgramTypeIcon type="Stream"/> Stream: video stream</li>
            <li><ProgramTypeIcon type="Text"/> Text: scrolling text</li>
        </ul>
    </div>
};

function App() {
    return <ThemeProvider theme={bhimaTheme}>
            <CssBaseline enableColorScheme />
            <Container sx={{ display: 'flex', justifyItems: "space-around", justifyContent: "center", padding: 0 }} className="App">
                <BrowserRouter>
                    <TopBar/>
                    <main>
                        <Routes>
                            <Route exact path="/" element={<HomePage/>}/>
                            <Route exact path="/settings" element={<SettingsPage/>}/>
                            <Route exact path="/wiring" element={<WiringPage/>}/>
                            <Route exact path="/programs" element={<ProgramsPage/>}/>
                        </Routes>
                    </main>
                </BrowserRouter>
            </Container>
        </ThemeProvider>;
}

export default App;
