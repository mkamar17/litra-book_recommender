import React, {useState, useEffect} from 'react';
import { useNavigate } from "react-router-dom";
import { getUserTotalPoints } from '../api/api.js';
import NotificationDropdown from "./NotificationDropdown";

import { Disclosure, DisclosureButton, DisclosurePanel, Menu, MenuButton, MenuItem, MenuItems } from '@headlessui/react'
import { Bars3Icon, BellIcon, XMarkIcon } from '@heroicons/react/24/outline'

import logo from '../assets/logo.png';
import pfp from '../assets/user_pfp.png';
import search from '../assets/search.png';

import StreakCalendar from "./StreakCalendar";


const navigation = [
  { name: 'Home', path: '/home' },
  { name: 'Genre', path: '/genre' },
  { name: 'My Library', path: '/library' },
]

function classNames(...classes) {
  return classes.filter(Boolean).join(' ')
}

export default function NavBar({ onSearch = () => {} }) { // to increase scalability add logic to App.jsx to handle search in all subpages
  const [query, setQuery] = useState('');
  const [totalPoints, setTotalPoints] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    fetchTotalPoints();

    const handlePointsUpdate = () => fetchTotalPoints();
    window.addEventListener('pointsUpdated', handlePointsUpdate);
  
    return () => window.removeEventListener('pointsUpdated', handlePointsUpdate);
  }, []);

  const fetchTotalPoints = async () => {
    try {
      const data = await getUserTotalPoints();
      setTotalPoints(data.totalPoints);
    } catch (error) {
      console.error('Failed to fetch total points:', error);
    }
  };

  useEffect(() => {
    if (query.trim() === '') {
      // when user clears the input, immediately reset
      onSearch('');
      return;
    }

    const delay = setTimeout(() => {
      onSearch(query);
    }, 400);

    return () => clearTimeout(delay);
  }, [query]);

  const handleSearch = (e) => {
    e.preventDefault();
  
    // if input is empty, reset 
    if (query.trim() === '') {
      onSearch(''); 
      return;
    }
  
    // otherwise search normally
    onSearch(query);
  };

  const handleSignOut = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    navigate("/");
  }
  
  return (
    <Disclosure
      as="nav"
      className="sticky top-0 z-50 bg-[rgb(41,40,40)] text-white after:pointer-events-none after:absolute after:inset-x-0 after:bottom-0 after:h-px after:bg-white/10"
    >
      <div className="w-full px-8">
        <div className="relative flex h-16 items-center justify-between">
          <div className="absolute inset-y-0 left-0 flex items-center sm:hidden">
            {/* Mobile menu button*/}
            <DisclosureButton className="group relative inline-flex items-center justify-center rounded-md p-2 text-gray-400 hover:bg-white/5 hover:text-white focus:outline-2 focus:-outline-offset-1 focus:outline-indigo-500">
              <span className="absolute -inset-0.5" />
              <span className="sr-only">Open main menu</span>
              <Bars3Icon aria-hidden="true" className="block size-6 group-data-open:hidden" />
              <XMarkIcon aria-hidden="true" className="hidden size-6 group-data-open:block" />
            </DisclosureButton>
          </div>
          <div className="flex flex-1 items-center justify-center sm:items-stretch sm:justify-start">
            <div className="flex shrink-0 items-center">
              <img
                src={logo}
                className="h-8 w-auto"
              />
            </div>
            <div className="hidden sm:ml-6 sm:block">
              <div className="flex space-x-4">
                  {navigation.map((item) => (
                    <button
                      key={item.name}
                      onClick={() => navigate(item.path)}
                      className={classNames(
                        'text-gray-300 hover:bg-white/5 hover:text-white rounded-md px-3 py-2 text-sm font-medium'
                      )}
                    >
                      {item.name}
                    </button>
                  ))}
              </div>
            </div>
          </div>
          
          <div className="flex items-center gap-4 ml-auto pr-6">
            
            {/* Search Bar */}
            <form onSubmit={handleSearch} className="relative">
            <input
              type="text"
              placeholder="Dystopian thriller novels..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="bg-[rgb(60,60,60)] text-white placeholder-gray-400 text-sm font-light rounded-full pl-10 pr-4 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-500 w-48 sm:w-64 transition-all duration-200"
            />
            <button
              type="submit"
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-white"
            >
              <img src={search} alt="search" className="w-4 h-4" />
            </button>
          </form>

          <div
            className="flex items-center gap-2 bg-[rgb(60,60,60)] rounded-full px-4 py-1.5 cursor-pointer hover:bg-[rgb(80,80,80)] transition"
            onClick={() => navigate("/leaderboard")}
          >
            <span className="text-yellow-400 text-lg">⭐</span>
            <span className="text-white font-semibold text-sm">{totalPoints.toLocaleString()}</span>
          </div>

          <StreakCalendar/>

            <NotificationDropdown/>

            {/* Profile dropdown */}
            <Menu as="div" className="relative -ml-2">
              <MenuButton className="relative flex rounded-full focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-500">
                <span className="absolute -inset-1.5" />
                <span className="sr-only">Open user menu</span>
                <img
                  alt=""
                  src={pfp}
                  className="size-8 rounded-full bg-gray-800 outline -outline-offset-1 outline-white/10"
                />
              </MenuButton>

              <MenuItems
                transition
                className="absolute right-0 z-10 mt-2 w-48 origin-top-right rounded-md bg-gray-800 py-1 outline -outline-offset-1 outline-white/10 transition data-closed:scale-95 data-closed:transform data-closed:opacity-0 data-enter:duration-100 data-enter:ease-out data-leave:duration-75 data-leave:ease-in"
              >
                <MenuItem>
                  <a
                    href="#"
                    onClick={() => navigate("/profile")}
                    className="block px-4 py-2 text-sm text-gray-300 data-focus:bg-white/5 data-focus:outline-hidden"
                  >
                    Your profile
                  </a>
                </MenuItem>
                <MenuItem>
                  <a
                    href="#"
                    className="block px-4 py-2 text-sm text-gray-300 data-focus:bg-white/5 data-focus:outline-hidden"
                  >
                    Settings
                  </a>
                </MenuItem>
                <MenuItem>
                  <a
                    href="#"
                    onClick={handleSignOut}
                    className="block px-4 py-2 text-sm text-gray-300 data-focus:bg-white/5 data-focus:outline-hidden"
                  >
                    Sign out
                  </a>
                </MenuItem>
              </MenuItems>
            </Menu>
          </div>
        </div>
      </div>
         <DisclosurePanel className="sm:hidden">
          <div className="space-y-1 px-2 pt-2 pb-3">
            {navigation.map((item) => (
              <DisclosureButton
                key={item.name}
                as="button"
                onClick={() => navigate(item.path)}
                className={classNames(
                  'block rounded-md px-3 py-2 text-base font-medium text-gray-300 hover:bg-white/5 hover:text-white'
                )}
              >
                {item.name}
              </DisclosureButton>
            ))}
          </div>
        </DisclosurePanel>
      </Disclosure>
  )
}