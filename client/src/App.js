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
    DialogTitle,
    FormControlLabel,
    IconButton,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    Paper,
    Slider,
    Stack,
    Switch,
    ThemeProvider,
    Toolbar
} from "@mui/material";
import {
    AccessTime,
    AppRegistration,
    Apps,
    BrightnessHigh,
    BrightnessLow,
    Cable,
    ConnectedTv,
    DirectionsBus,
    Image, Info,
    Label,
    LocalMovies,
    QuestionMark,
    Settings,
    TextFields,
    TextRotationNone
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

/** Converts seconds to human-readable duration format */
function secondsToHuman(totalSecs) {
    let days    = Math.floor(totalSecs / 86400);
    let hours   = Math.floor((totalSecs - (days * 86400)) / 3600);
    let minutes = Math.floor((totalSecs - (days * 86400) - (hours * 3600)) / 60);
    let seconds = totalSecs - (days * 86400) - (hours * 3600) - (minutes * 60);

    let daysStr = (days > 0) ? (days + " days ") : ("");
    if (hours   < 10) {hours   = "0"+hours;}
    if (minutes < 10) {minutes = "0"+minutes;}
    if (seconds < 10) {seconds = "0"+seconds;}
    return daysStr + hours+'h '+minutes+'m '+seconds + "s";
}


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
    const {type, fontSize} = props;
    switch(type) {
        case TYPE_MOVIE: return <LocalMovies className="programType" fontSize={fontSize}/>;
        case TYPE_ALGORITHM: return <Apps className="programType" fontSize={fontSize} />;
        case TYPE_IMAGE: return <Image className="programType" fontSize={fontSize} />;
        case TYPE_STREAM: return <ConnectedTv className="programType" fontSize={fontSize} />;
        case TYPE_TEXT: return <TextFields className="programType" fontSize={fontSize} />;
        default: return <QuestionMark className="programType" fontSize={fontSize} />;
    }
};

const TopBar = (props) => {
    // TODO highlight current page
    // TODO make topbar slide in/out on scroll
    return <AppBar position="fixed" color="primary" title="Bhima">
        <Toolbar sx={{display: "flex", flexDirection: "row", justifyContent: "space-evenly", justifyItems: "center"}}>
            <Box
                component="img"
                sx={{maxHeight: 50, mr: 2}}
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
            <NavLink to="/system" end className={(props) => `${props.isActive ? 'active ' : ''}`} >
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

function NetworkError(error) {
    return <Alert severity="error">Dragonmind link failure: {error.message}</Alert>;
}

const CurrentProgram = (props) => {

    const {program, setProgram} = props;
    const [loaded, setLoaded] = useState(false);

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
        return NetworkError(error);
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
    const {program, setProgram} = props;

    let [open, setOpen] = React.useState(false);

    const handleOpen = () => {
        setOpen(true);
    }
    const handleCancel = () => {
        setOpen(false);
    }
    const handleRun = () => {
        setOpen(false);
        let data = new FormData();
        data.append("id", program.id);
        fetch(getEndpoint("runProgram2?id=" + encodeURI(program.id)), {
            method: 'POST',
            headers: getHeaders()
        }).then(res => res.json())
            .then(() => {
                setProgram(program);
                window.scrollTo(0, 0);
            });
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
            <ProgramTypeIcon type={program.type.name} fontSize="large"/>
            <span className="programName">{program.name}</span>
        </Paper>
    </React.Fragment>;
};

const ProgramList = (props) => {
    const {setProgram} = props;
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
                    <ProgramCard key={program.id} program={program} setProgram={setProgram}/>
                ))}
            </Stack>
        );
    }
};

const HomePage = (props) => {
    const [program, setProgram] = useState(null);

    return (
        <Container className="page">
            <h3>Live</h3>
            <CurrentProgram program={program} setProgram={setProgram}/>
            <h3>All Programs</h3>
            <ProgramList setProgram={setProgram}/>
        </Container>
    );
};


const WiringPage = (props) => {
    return (
    <Container className="page">
        <h3>Wiring</h3>
        This is where the wiring page goes, yo.
    </Container>);
};

const SystemPage = (props) => {
    const {systemInfo, error, loaded} = props;
    if (error) {
        return NetworkError(error);
    } else if (!loaded) {
        return <CircularProgress color="secondary" />;
    } else {
        const updateBrightness = () => {}; // TODO update brightness
        return (
            <Container className="page">
                <h3>System</h3>
                <List>
                    <ListItem>
                        <ListItemIcon>
                            <Info fontSize="large"/>
                        </ListItemIcon>
                        <ListItemText primary="Status" secondary={systemInfo.status} />
                    </ListItem>                    <ListItem>
                        <ListItemIcon>
                            <Label fontSize="large"/>
                        </ListItemIcon>
                        <ListItemText primary="Version" secondary={systemInfo.version} />
                    </ListItem>
                    <ListItem>
                        <ListItemIcon>
                            <AccessTime fontSize="large"/>
                        </ListItemIcon>
                        {/* TODO update this client-side on a timer*/}
                        <ListItemText primary="Uptime" secondary={secondsToHuman(systemInfo.uptimeSeconds)} />
                    </ListItem>
                    <ListItem>
                        <ListItemIcon>
                            <TextRotationNone fontSize="large"/>
                        </ListItemIcon>
                        <ListItemText primary="Scroll Text" secondary={systemInfo.scrollText} />
                    </ListItem>
                    <ListItem>
                        <ListItemIcon>
                            <ProgramTypeIcon type={systemInfo.currentProgram.type.name} fontSize="large"/>
                        </ListItemIcon>
                        <ListItemText primary="Current Program" secondary={systemInfo.currentProgram.name} />
                    </ListItem>


                {/*    TODO: RAM, disk, load, temp, */}
                </List>
                <h3>Global Settings</h3>
                <Stack>
                    <FormControlLabel control={<Switch checked={systemInfo.settings.luminanceCorrection}/>} label="Gamma Correction" />
                    <FormControlLabel control={<Switch checked={systemInfo.settings.autoThrottle}/>} label="Autothrottle" />
                    <FormControlLabel control={<Switch checked={systemInfo.settings.mute}/>} label="Mute" />
                    <Stack spacing={2} direction="row" sx={{ mb: 1, width: "90%" }} alignItems="center">
                        <BrightnessLow />
                        <Slider aria-label="Brightness" value={systemInfo.settings.brightness} onChange={updateBrightness} />
                        <BrightnessHigh />
                    </Stack>
                </Stack>
                <h3>Operations</h3>
                <Box sx={{display: 'flex', flexDirection: 'row', justifyContent: 'space-evenly', mt: 2}}>
                    {/*TODO if asleep, show "Wake Up"*/}
                    <Button variant="contained" href="#sleep">
                        Sleep
                    </Button>
                    <Button variant="contained" href="#puge-cache">
                        Purge Cache
                    </Button>
                </Box>
            </Container>
        );
    }
};

const ProgramsPage = (props) => {
    const {systemInfo, error, loaded} = props;
    if (error) {
        return NetworkError(error);
    } else if (!loaded) {
        return <CircularProgress color="secondary"/>;
    } else {
        return <Container className="page">
            <h3>Program Configuration</h3>
            <ul className="programTypes">
                {systemInfo.programTypes.map(pt => (
                    <li key={pt.name}><ProgramTypeIcon type={pt.name} fontSize="large"/> {pt.name}: {pt.description}</li>
                ))}
            </ul>
        </Container>;
    }
};

function App() {
    const [systemInfo, setSystemInfo] = useState(null);
    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState(null);
    useEffect(() => {
        bhimaFetch("systemInfo").then(
            (result) => {
                setLoaded(true);
                setSystemInfo(result);
            },
            (error) => {
                setLoaded(true);
                setError(error);
            }
        );
    }, []);
    return <ThemeProvider theme={bhimaTheme}>
            <CssBaseline enableColorScheme />
            <Container sx={{ display: 'flex', justifyItems: "space-around", justifyContent: "center", padding: 0 }} className="App">
                <BrowserRouter>
                    <TopBar/>
                    <main>
                        <Routes>
                            <Route exact path="/" element={<HomePage systemInfo={systemInfo} error={error} loaded={loaded}/>}/>
                            <Route exact path="/system" element={<SystemPage systemInfo={systemInfo} error={error} loaded={loaded}/>}/>
                            <Route exact path="/wiring" element={<WiringPage systemInfo={systemInfo} error={error} loaded={loaded}/>}/>
                            <Route exact path="/programs" element={<ProgramsPage systemInfo={systemInfo} error={error} loaded={loaded}/>}/>
                        </Routes>
                    </main>
                </BrowserRouter>
            </Container>
        </ThemeProvider>;
}

export default App;
