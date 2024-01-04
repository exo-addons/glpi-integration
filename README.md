# Glpi Integration Addon
This addon is a connector for [GLPI Server](https://glpi-project.org/fr/) allowing to read an display tickets from GLPI in eXo Platform instance.

## Install

To install this addon, launch the command : 
```
cd ${EXO_HOME}
./addon install exo-glpi-integration
```

Then start the server

## Configure the GLPI Gadget 

### Add the application into the app registry

Access to Site Platform Settings, and choose menu WCM Components.
Add the application GLPI Integration into a category :

![Capture 2024-01-03 at 18 20 17](https://github.com/exo-addons/glpi-integration/assets/807839/2a0f9b6a-cf99-4de1-ab4a-6d98ea471cc9)

### Add the aplication into a page

As an administrator, edit the layout of a page and add the portlet GLPI

### Configure the general settings of the portlet

As an administrator, you must enter these informations once :

- Server adress : `https//(domain)/apirest.php`
- App token
- Limit the number of tickets to list : Sets the maximum number of tickets to be displayed in the first gadget

The application token can be created in Glpi server : as administrator, go to Setup > General > API, then create a new 'Api Client'. It will provide you a token, you will have to put in the application configuration on eXo side.

![Untitled](https://github.com/exo-addons/glpi-integration/assets/807839/1d36a50d-cad5-4275-a351-09ee031ab781)

## Connect eXo with your account GLPI

All users can now link eXo to their GLPI profile.

1. In GLPI, Go to your preferences and generate your remote access key : My Settings > Remote Access Keys 

![Untitled (1)](https://github.com/exo-addons/glpi-integration/assets/807839/a7b6061a-7f2a-4041-a1fc-d16afb0743ba)

2. Then paste the token in the field token 

![Untitled (2)](https://github.com/exo-addons/glpi-integration/assets/807839/2c094fb1-fc76-4e72-95b4-9604803f6189)


The connector retrieves all the user's active requests and requests with closed status less than 30 days old.

