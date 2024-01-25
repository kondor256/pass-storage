import React, { useState,useEffect } from 'react';
import Typography from "@mui/material/Typography";
import {Button, Divider, FormControl, IconButton, Input, InputAdornment, InputLabel, TextField} from "@mui/material";
import Box from "@mui/material/Box";

import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import VisibilityIcon from "@mui/icons-material/Visibility";

import {styleModal} from "./StyleModal";

//function FormPassword(props){
export const FormPassword = React.forwardRef((props, ref) => {
    const [pass, setPass] = useState({...props.pass});

    const [showPassword, setShowPassword] = useState(false);
    const handleClickShowPassword = () => setShowPassword((show) => !show);
    const handleMouseDownPassword = (event) => {
        event.preventDefault();
    };

    const editInfobaseOK = () => {
        Object.assign(props.pass, pass);
        props.handleSavePass(pass);
        props.handleClose();
    }

    return(
        <Box sx={styleModal}>
            <Typography variant="h4">Password</Typography>
            <TextField id="valUrl" label="URL" variant="standard" fullWidth={true}
                       defaultValue={pass.url}
                       onChange={(e) => {pass.url = e.target.value}} />
            <TextField id="valLogin" label="User name" variant="standard" fullWidth={true}
                       defaultValue={pass.login}
                       onChange={(e) => {pass.login = e.target.value}} />
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
                <Button variant="outlined" onClick={editInfobaseOK}>OK</Button>
                <Button variant="outlined" onClick={props.handleClose}>Cancel</Button>
            </Box>
        </Box>
    )
})
