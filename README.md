# Rooms
A small web app to find currently available group rooms Chalmers campus.
The prototype is currently hosted on [edvin.ga/rooms](https://edvin.ga/rooms) but will
soon move to [dtek.se](https://dtek.se).

Winner of Ericsson + DAG Chalmers Hackathon 2021.

Created by Edvin Nilsson, Jonathan Widén, Eli Adelhult, Leopold Wigbratt, and Alex Berglund.

# Hosting the service
Instructions about the back-end can be found inside a seperate readme file in the `backend` directory.

To start the front-end run `npm run start` or `npm run build`. 

Make sure to set the env variable `REACT_APP_DOORS_BACKEND_URL` to the correct api entry point as well.  

The web app has two different display modes. To show a small preview (intended for iframes) instead of the full app add `/#preview` to the URL. 