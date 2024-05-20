import React, { useState,useEffect } from 'react';

import {IconButton, Link, ListItem, ListItemIcon} from "@mui/material";
import ListItemText from "@mui/material/ListItemText";
import ListItemButton from "@mui/material/ListItemButton";

import ShareIcon from '@mui/icons-material/Share';
import LockIcon from '@mui/icons-material/Lock';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';

import {copyToClipboard} from "./copyToClipboard";

function PasswordString(props){
    const copyPassToClipboard = () => {
        copyToClipboard(props.pass.password).then(() => props.handleShowMessage("Password copied!"));
    }
    const isURLValid = (url) => {
        try {
            const testUrl = new URL(url);
        } catch (e) {
            return false;
        }
        return true;
    }

    return([
        <ListItem key={"passString"+props.pass.id}
            sx={{paddingTop: 0, paddingBottom: 0}}
            secondaryAction={[
                <IconButton disabled={props.pass.shared} key="buttonShare" edge="end" aria-label="share" sx={{marginRight: 0}}
                            onClick={props.onClickSharePass.bind(null, props.pass)}
                >
                    <ShareIcon />
                </IconButton>,
                <IconButton key="buttonCopy" edge="end" aria-label="copy"
                            onClick={copyPassToClipboard}
                >
                    <ContentCopyIcon />
                </IconButton>
            ]}
            disablePadding
        >
            <ListItemButton dense onDoubleClick={props.onClickPass.bind(null,props.pass)}>
                <ListItemIcon>
                    <LockIcon />
                </ListItemIcon>
                <ListItemText primary={props.pass.name}
                              secondary={props.pass.url && isURLValid(props.pass.url)?<Link href={props.pass.url} underline="hover">{props.pass.url}</Link>:null}/>
                {/*{props.pass.url && isURLValid(props.pass.url)?<ListItemText primary={<link href={props.pass.url}>props.pass.url</link>} primaryTypographyProps={{align: 'left'}}/>:null}*/}
            </ListItemButton>
        </ListItem>
    ])
}

export default PasswordString;