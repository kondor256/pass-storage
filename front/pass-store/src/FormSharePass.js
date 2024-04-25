import React, { useState,useEffect } from 'react';
import {styleModal} from "./StyleModal";
import Typography from "@mui/material/Typography";
import {Button, List, ListItem, ListItemIcon, ListItemButton, Checkbox, ListItemText} from "@mui/material";
import DeleteForeverIcon from "@mui/icons-material/DeleteForever";
import {red} from "@mui/material/colors";
import Box from "@mui/material/Box";
import { PASS_API } from "./ApiAddresses";

export const FormSharePass = React.forwardRef((props, ref) => {
    const [checked, setChecked] = useState([]);

    const handleToggle = (user) => () => {
        const currentIndex = checked.indexOf(user.id);
        const newChecked = [...checked];

        if (currentIndex === -1) {
            newChecked.push(user.id);
        } else {
            newChecked.splice(currentIndex, 1);
        }

        setChecked(newChecked);
    };
    useEffect(() => {
        fetch(PASS_API+"/shared_users/"+props.pass.id).then(resp => {
            if (resp.ok)
                resp.json().then(userList => {
                    const newChecked = [];
                    for (const user of userList){
                        newChecked.push(user.id);
                    }
                    setChecked(newChecked);
                });
            else resp.text().then(txt => console.log(txt));
        });
    },[])

    const sharePassOK = () => {

        const request = {
            passId: props.pass.id,
            userIdList: checked
        };
        fetch(PASS_API+"/share",
            {method: 'POST',
                headers: {
                    'Content-Type': 'application/json;charset=utf-8'
                },
                body: JSON.stringify(request)
            }).then(responce => {
            if (responce.ok){

            } else {
                responce.text().then(txt => console.log(txt));
            }
        })

        props.handleClose();
    }

    return(
        <Box sx={styleModal}>
            <Typography variant="h4">Share password "{props.pass.description}"</Typography>
            <List>
                {props.userList.map((user) => {
                    const labelId = "list-item-"+user.id;

                    return(
                        <ListItem key={user.id}
                                  sx={{paddingTop: 0, paddingBottom: 0}}
                                  disablePadding
                        >
                            <ListItemButton onClick={handleToggle(user)}
                                            dense>
                                <ListItemIcon>
                                    <Checkbox edge="start"
                                              checked={checked.indexOf(user.id) !== -1}
                                              disableRipple
                                              inputProps={{"aria-labelledby": labelId}}
                                              sx={{paddingTop: 0, paddingBottom: 0}}
                                    />
                                </ListItemIcon>
                                <ListItemText id={labelId} primary={`${user.name} (${user.login})`}/>
                            </ListItemButton>
                        </ListItem>
                    );
                })}
            </List>
            <Box sx={{ m: 1 }}>
                <Button variant="outlined" onClick={sharePassOK}>OK</Button>
                <Button variant="outlined" onClick={props.handleClose}>Cancel</Button>
                {/*{newPass?null:<Button variant="outlined" startIcon={<DeleteForeverIcon sx={{color: red[700]}}/>} onClick={deletePass}>DELETE</Button>}*/}
            </Box>
        </Box>
    )
})