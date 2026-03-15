import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import {
  getFriends,
  getPendingRequests,
  searchUsers,
  sendFriendRequest,
  acceptFriendRequest,
  removeFriend,
  updateUsername,
} from "../api/api.js";
import "../styles/ProfilePage.css";
import NavBar from "../components/NavBar";

const getInitial = (email) => (email ? email[0].toUpperCase() : "?");
const getUsername = (email) => (email ? email.split("@")[0] : "");

export default function ProfilePage() {
  const navigate = useNavigate();
  const currentEmail = localStorage.getItem("email");
  const fileInputRef = useRef(null);

  const [tab, setTab] = useState("friends");

  // Friends state
  const [friends, setFriends] = useState([]);
  const [pending, setPending] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [sentRequests, setSentRequests] = useState(new Set());

  // Settings state
  const [username, setUsername] = useState(
    localStorage.getItem("username") || getUsername(currentEmail)
  );
  const [usernameInput, setUsernameInput] = useState(username);
  const [savingUsername, setSavingUsername] = useState(false);
  const [usernameSaved, setUsernameSaved] = useState(false);
  const [profilePic, setProfilePic] = useState(
    localStorage.getItem("profilePic") || null
  );

  useEffect(() => {
    loadFriends();
    loadPending();
  }, []);

  async function loadFriends() {
    try {
      const data = await getFriends();
      setFriends(data);
    } catch (e) {
      console.error(e);
    }
  }

  async function loadPending() {
    try {
      const data = await getPendingRequests();
      setPending(data);
    } catch (e) {
      console.error(e);
    }
  }

  useEffect(() => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      return;
    }
    const t = setTimeout(async () => {
      setSearching(true);
      try {
        const results = await searchUsers(searchQuery);
        // filter out self and existing friends
        const friendEmails = new Set(friends.map((f) => f.email));
        setSearchResults(
          results.filter(
            (u) => u.email !== currentEmail && !friendEmails.has(u.email)
          )
        );
      } catch (e) {
        console.error(e);
      } finally {
        setSearching(false);
      }
    }, 400);
    return () => clearTimeout(t);
  }, [searchQuery, friends]);

  async function handleSendRequest(userId) {
    try {
      await sendFriendRequest(userId);
      setSentRequests((prev) => new Set([...prev, userId]));
    } catch (e) {
      console.error(e);
    }
  }

  async function handleAccept(friendshipId) {
    try {
      await acceptFriendRequest(friendshipId);
      await loadFriends();
      await loadPending();
    } catch (e) {
      console.error(e);
    }
  }

  async function handleRemove(friendshipId) {
    try {
      await removeFriend(friendshipId);
      await loadFriends();
    } catch (e) {
      console.error(e);
    }
  }

  async function handleSaveUsername() {
    if (!usernameInput.trim() || usernameInput === username) return;
    setSavingUsername(true);
    try {
      await updateUsername(usernameInput.trim());
      setUsername(usernameInput.trim());
      localStorage.setItem("username", usernameInput.trim());
      setUsernameSaved(true);
      setTimeout(() => setUsernameSaved(false), 2500);
    } catch (e) {
      console.error(e);
    } finally {
      setSavingUsername(false);
    }
  }

  function handleProfilePicChange(e) {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (ev) => {
      const dataUrl = ev.target.result;
      setProfilePic(dataUrl);
      localStorage.setItem("profilePic", dataUrl);
    };
    reader.readAsDataURL(file);
  }

  return (
    //<NavBar />
    <div className="profile-page">
        
      {/* Header */}
      <div className="profile-header">
        <div className="profile-avatar-wrap">
          <div
            className="profile-avatar"
            onClick={() => fileInputRef.current?.click()}
          >
            {profilePic ? (
              <img src={profilePic} alt="profile" className="profile-avatar-img" />
            ) : (
              <span className="profile-avatar-initial">
                {getInitial(currentEmail)}
              </span>
            )}
            <div className="profile-avatar-overlay">
              <span>📷</span>
            </div>
          </div>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            className="hidden"
            onChange={handleProfilePicChange}
          />
        </div>
        <div className="profile-header-info">
          <h1 className="profile-name">{username}</h1>
          <p className="profile-email">{currentEmail}</p>
        </div>
      </div>

      {/* Tabs */}
      <div className="profile-tabs">
        <button
          className={`profile-tab ${tab === "friends" ? "profile-tab--active" : ""}`}
          onClick={() => setTab("friends")}
        >
          👥 Friends {friends.length > 0 && <span className="profile-tab-badge">{friends.length}</span>}
        </button>
        <button
          className={`profile-tab ${tab === "settings" ? "profile-tab--active" : ""}`}
          onClick={() => setTab("settings")}
        >
          ⚙️ Settings
        </button>
      </div>

      {/* Friends Tab */}
      {tab === "friends" && (
        <div className="profile-section">
          {/* Search */}
          <div className="profile-search-wrap">
            <input
              className="profile-search"
              placeholder="Search users by email..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          {/* Search Results */}
          {searchResults.length > 0 && (
            <div className="profile-list-section">
              <h3 className="profile-list-title">Search Results</h3>
              {searchResults.map((u) => (
                <div key={u.id} className="profile-friend-row">
                  <div className="profile-friend-avatar">{getInitial(u.email)}</div>
                  <div className="profile-friend-info">
                    <p className="profile-friend-name">{getUsername(u.email)}</p>
                    <p className="profile-friend-email">{u.email}</p>
                  </div>
                  <button
                    className={`profile-action-btn ${sentRequests.has(u.id) ? "profile-action-btn--sent" : "profile-action-btn--add"}`}
                    onClick={() => handleSendRequest(u.id)}
                    disabled={sentRequests.has(u.id)}
                  >
                    {sentRequests.has(u.id) ? "Sent ✓" : "+ Add"}
                  </button>
                </div>
              ))}
            </div>
          )}

          {searching && <p className="profile-hint">Searching...</p>}
          {searchQuery && !searching && searchResults.length === 0 && (
            <p className="profile-hint">No users found.</p>
          )}

          {/* Pending Requests */}
          {pending.length > 0 && (
            <div className="profile-list-section">
              <h3 className="profile-list-title">
                Pending Requests
                <span className="profile-tab-badge">{pending.length}</span>
              </h3>
              {pending.map((req) => (
                <div key={req.id} className="profile-friend-row">
                  <div className="profile-friend-avatar profile-friend-avatar--pending">
                    {getInitial(req.requesterEmail)}
                  </div>
                  <div className="profile-friend-info">
                    <p className="profile-friend-name">{getUsername(req.requesterEmail)}</p>
                    <p className="profile-friend-email">{req.requesterEmail}</p>
                  </div>
                  <button
                    className="profile-action-btn profile-action-btn--accept"
                    onClick={() => handleAccept(req.id)}
                  >
                    Accept
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* Friends List */}
          <div className="profile-list-section">
            <h3 className="profile-list-title">My Friends</h3>
            {friends.length === 0 ? (
              <p className="profile-hint">No friends yet. Search above to add some!</p>
            ) : (
              friends.map((f) => (
                <div key={f.id} className="profile-friend-row">
                  <div className="profile-friend-avatar">{getInitial(f.email)}</div>
                  <div className="profile-friend-info">
                    <p className="profile-friend-name">{getUsername(f.email)}</p>
                    <p className="profile-friend-email">{f.email}</p>
                  </div>
                  <button
                    className="profile-action-btn profile-action-btn--remove"
                    onClick={() => handleRemove(f.friendshipId)}
                  >
                    Remove
                  </button>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* Settings Tab */}
      {tab === "settings" && (
        <div className="profile-section">
          <div className="profile-list-section">
            <h3 className="profile-list-title">Display Name</h3>
            <p className="profile-hint">This is how your name appears on the leaderboard and to friends.</p>
            <div className="profile-username-row">
              <input
                className="profile-username-input"
                value={usernameInput}
                onChange={(e) => setUsernameInput(e.target.value)}
                maxLength={30}
                placeholder="Enter username..."
              />
              <button
                className="profile-action-btn profile-action-btn--save"
                onClick={handleSaveUsername}
                disabled={savingUsername || usernameInput === username}
              >
                {savingUsername ? "Saving..." : usernameSaved ? "Saved ✓" : "Save"}
              </button>
            </div>
          </div>

          <div className="profile-list-section">
            <h3 className="profile-list-title">Profile Picture</h3>
            <p className="profile-hint">Stored locally on your device.</p>
            <div className="profile-pic-preview-row">
              <div className="profile-pic-preview">
                {profilePic ? (
                  <img src={profilePic} alt="profile" className="profile-avatar-img" />
                ) : (
                  <span className="profile-avatar-initial">{getInitial(currentEmail)}</span>
                )}
              </div>
              <div className="profile-pic-actions">
                <button
                  className="profile-action-btn profile-action-btn--add"
                  onClick={() => fileInputRef.current?.click()}
                >
                  {profilePic ? "Change Photo" : "Upload Photo"}
                </button>
                {profilePic && (
                  <button
                    className="profile-action-btn profile-action-btn--remove"
                    onClick={() => {
                      setProfilePic(null);
                      localStorage.removeItem("profilePic");
                    }}
                  >
                    Remove
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}