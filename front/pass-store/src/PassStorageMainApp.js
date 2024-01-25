import React, { useState,useEffect } from 'react';
import {createRoot} from "react-dom/client";

import Box from '@mui/material/Box';
import Drawer from '@mui/material/Drawer';
import AppBar from '@mui/material/AppBar';
import CssBaseline from '@mui/material/CssBaseline';
import Toolbar from '@mui/material/Toolbar';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import MyPasswords from "./MyPasswords";

function PassStorageMainApp(props){
    const [curPage,setCurPage] = useState(0);
    const [tabList,setTabList] = useState([]);

    const drawerWidth = 240;

    useEffect(() => {

        setTabList(['My passwords']);
        setCurPage(1);
    },[])

    return(
        <Box key="boxMainApp" sx={{ display: 'flex' }}>
            <CssBaseline />
            <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
                <Toolbar>
                    <Typography variant="h6" noWrap component="div">
                        Passwords storage
                    </Typography>
                </Toolbar>
            </AppBar>
            <Drawer
                variant="permanent"
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: 'border-box' },
                }}
            >
                <Toolbar />
                <Box sx={{ overflow: 'auto' }}>
                    <List>
                        {tabList.map((text, index) => (
                            <ListItem key={text} disablePadding>
                                <ListItemButton selected={curPage === index+1} onClick={(e) => {setCurPage(index+1)}}>
                                    <ListItemText primary={text} />
                                </ListItemButton>
                            </ListItem>
                        ))}
                    </List>
                </Box>
            </Drawer>
            <Box display={curPage === 0?'':'none'} component="main" sx={{ flexGrow: 1, p: 3 }}>
                <Toolbar />
            </Box>
            <Box display={curPage === 1?'':'none'} component="main" sx={{ flexGrow: 1, p: 3, bgcolor: '#fafafa' }}>
                <Toolbar />
                <MyPasswords />
            </Box>
        </Box>
    )
}

const rootEl = document.getElementById('PassStorageMainApp');
if (rootEl) createRoot(rootEl).render(<PassStorageMainApp />);