import React, { useState,useEffect } from 'react';
import {Button, Collapse, Divider, Grid, IconButton, ListItem, ListItemIcon, Modal} from "@mui/material";

import List from '@mui/material/List';
import ListItemButton from "@mui/material/ListItemButton";
import ListItemText from "@mui/material/ListItemText";

import FolderIcon from '@mui/icons-material/Folder';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ControlPointIcon from '@mui/icons-material/ControlPoint';

import PasswordString from "./PasswordString";
import { FormPassword } from "./FormPassword";
import {green, red} from "@mui/material/colors";
import ShareIcon from "@mui/icons-material/Share";

const PASS_API = "/api/v1/pass";

function MyPasswords(props){
    const [passList, setPassList] = useState([]);
    const [editedPass,setEditedPass] = useState(null);
    const [open, setOpen] = React.useState(false);

    const updatePassList = () => {
        fetch(PASS_API+"/list").then(resp => resp.json()).then(passList => {
            const passes = [];
            for (const pass of passList) {
                passes.push(pass);
            }
            setPassList(passes);
        })
    }

    useEffect(() => {
        updatePassList();
    },[])

    const handlePassFormClose = () => {
        setEditedPass(null);
    }

    const createPass = () => {
        fetch(PASS_API+"/empty").then(resp => resp.json()).then(newPass => {
            setEditedPass(newPass);
        })
    }

    const postPass = (pass) => {
        fetch(PASS_API,{
            method: 'POST',
            headers: {
                'Content-Type': 'application/json;charset=utf-8'
            },
            body: JSON.stringify(pass)
        }).then(responce => {
            if (responce.ok){
                updatePassList();
            } else {
                console.log(responce);
            }
        });
    }

    const handleClick = () => {
        setOpen(!open);
    };

    return([
        <Grid key={"gridMyPasswords"}
            container spacing={2}>
            <Grid item xs={3}>
                <List
                    sx={{ width: '100%', bgcolor: 'background.paper', borderRadius: 2 }}
                >
                    <ListItem key="1" sx={{paddingTop: 0, paddingBottom: 0}}
                              secondaryAction={[
                                  <IconButton key="buttonShare" edge="end" aria-label="share" sx={{marginRight: 0}}>
                                      <ControlPointIcon sx={{color: green[700]}}/>
                                  </IconButton>,
                                  open ? <ExpandLessIcon key="buttonExpand" onClick={handleClick}/> : <ExpandMoreIcon key="buttonExpand" onClick={handleClick}/>
                              ]}
                    >
                        <ListItemButton dense >
                            <ListItemIcon>
                                <FolderIcon />
                            </ListItemIcon>
                            <ListItemText primary="All" />

                        </ListItemButton>
                    </ListItem>
                    <Collapse in={open} timeout="auto" unmountOnExit>
                        <ListItem key="2" sx={{paddingTop: 0, paddingBottom: 0}}
                                  secondaryAction={[
                                      <IconButton key="buttonShare" edge="end" aria-label="share" sx={{marginRight: 0}}>
                                          <ControlPointIcon sx={{color: green[700]}}/>
                                      </IconButton>
                                  ]}
                        >
                            <ListItemButton dense sx={{ pl: 4 }}>
                                <ListItemIcon>
                                    <FolderIcon />
                                </ListItemIcon>
                                <ListItemText primary="Internal" />
                            </ListItemButton>
                        </ListItem>
                    </Collapse>
                </List>
            </Grid>
            <Grid item xs={9}>
                <List
                    sx={{ width: '100%', bgcolor: 'background.paper', borderRadius: 2 }}
                >
                    {passList.map((pass) =>
                       <PasswordString key={"pass"+pass.id}
                                       text={pass.description} pass={pass} onClickPass={(pass) => {setEditedPass(pass)}}/>
                    )}
                </List>
                <Divider sx={{margin: 1, border: 0}} />
                <Button variant="outlined" disabled={false} onClick={createPass}>Create password</Button>
            </Grid>
        </Grid>,
        <Modal
            key="PassForm"
            open={editedPass != null}
            onClose={handlePassFormClose}
        >
            <FormPassword pass={editedPass}
                          handleClose={handlePassFormClose}
                          handleSavePass={postPass}/>
        </Modal>
    ])
}

export default MyPasswords;