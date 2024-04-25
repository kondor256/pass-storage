import React, { useState,useEffect } from 'react';
import Typography from "@mui/material/Typography";
import {
    Autocomplete,
    Button,
    Divider,
    FormControl,
    IconButton,
    Input,
    InputAdornment,
    InputLabel,
    TextField
} from "@mui/material";
import Box from "@mui/material/Box";
import {red} from "@mui/material/colors";

import DeleteForeverIcon from "@mui/icons-material/DeleteForever";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import VisibilityIcon from "@mui/icons-material/Visibility";

import {styleModal} from "./StyleModal";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import {copyToClipboard} from "./copyToClipboard";

export const FormPassword = React.forwardRef((props, ref) => {
    const [pass, setPass] = useState({...props.pass});
    const [parentFolder, setParentFolder] = useState(null);
    const newPass = props.pass.id == null;

    const [showPassword, setShowPassword] = useState(false);
    const handleClickShowPassword = () => setShowPassword((show) => !show);
    const handleMouseDownPassword = (event) => {
        event.preventDefault();
    };

    useEffect(()=>{
        for (const pFolder of props.folderList) {
            if (pass.folderId === pFolder.id || (pass.folderId == null && pFolder.id === "0"))
                setParentFolder(pFolder);
        }
    },[])

    const editPassOK = () => {
        if (parentFolder && parentFolder.id === "0")
            pass.folderId = null;
        else
            pass.folderId = parentFolder.id;

        Object.assign(props.pass, pass);
        props.handleSavePass(pass);
        props.handleClose();
    }
    const deletePass = () => {
        props.handleDeletePass(pass);
        props.handleClose();
    }

    const copyLoginToClipboard = () => {
        copyToClipboard(pass.login).then(() => props.handleShowMessage("Login copied!"));
    }
    const copyURLToClipboard = () => {
        copyToClipboard(pass.url).then(() => props.handleShowMessage("Address copied!"));
    }
    const copyPassToClipboard = () => {
        copyToClipboard(pass.password).then(() => props.handleShowMessage("Password copied!"));
    }

    return(
        <Box sx={styleModal}>
            <Typography variant="h4">Password</Typography>
            <Autocomplete
                disabled={pass.shared}
                disablePortal
                value={parentFolder}
                renderInput={(params) => <TextField {...params}  label="Parent folder" variant="standard" fullWidth={true} />}
                onChange={(e,v) => {
                    setParentFolder(v)}}
                options={props.folderList}
                getOptionLabel={folder => folder.name}
                isOptionEqualToValue={(o,v) => (o.id === v.id)}/>
            <Divider sx={{margin: 1, border: 0}} />
            <TextField id="valUrl" label="URL" variant="standard" fullWidth={true}
                       defaultValue={pass.url}
                       onChange={(e) => {pass.url = e.target.value}}
                       InputProps={{endAdornment: <InputAdornment position="end">
                               <IconButton key="buttonCopy" edge="end"
                                           onClick={copyURLToClipboard}
                               >
                                   <ContentCopyIcon />
                               </IconButton>
                           </InputAdornment>}}
            />
            <TextField id="valLogin" label="User name" variant="standard" fullWidth={true}
                       defaultValue={pass.login}
                       onChange={(e) => {pass.login = e.target.value}}
                       InputProps={{endAdornment: <InputAdornment position="end">
                               <IconButton key="buttonCopy" edge="end"
                                           onClick={copyLoginToClipboard}
                               >
                                   <ContentCopyIcon />
                               </IconButton>
                           </InputAdornment>}}
            />
            <FormControl fullWidth variant="standard">
                <InputLabel htmlFor="valPassword">Password</InputLabel>
                <Input
                    id="valPassword"
                    defaultValue={pass.password}
                    onChange={(e) => {pass.password = e.target.value}}
                    type={showPassword ? 'text' : 'password'}
                    endAdornment={
                        <InputAdornment position="end">
                            <IconButton
                                aria-label="toggle password visibility"
                                onClick={handleClickShowPassword}
                                onMouseDown={handleMouseDownPassword}
                            >
                                {showPassword ? <VisibilityOffIcon /> : <VisibilityIcon />}
                            </IconButton>
                            <IconButton key="buttonCopy" edge="end"
                                        onClick={copyPassToClipboard}
                            >
                                <ContentCopyIcon />
                            </IconButton>
                        </InputAdornment>
                    }
                />
            </FormControl>
            <TextField id="valDescription" label="Description" variant="standard" fullWidth={true}
                       defaultValue={pass.description}
                       multiline rows={5}
                       onChange={(e) => {pass.description = e.target.value}} />
            <Divider sx={{margin: 1, border: 0}} />
            <Box sx={{ m: 1 }}>
                <Button disabled={pass.shared} variant="outlined" onClick={editPassOK}>OK</Button>
                <Button variant="outlined" onClick={props.handleClose}>Cancel</Button>
                {newPass || pass.shared?null:<Button variant="outlined" startIcon={<DeleteForeverIcon sx={{color: red[700]}}/>} onClick={deletePass}>DELETE</Button>}
            </Box>
        </Box>
    )
})
