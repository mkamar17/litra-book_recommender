import React, { useState, useRef, useEffect } from "react";
import { BellIcon } from "@heroicons/react/24/outline";
import { getNotifications, markAllNotificationsRead } from "../api/api.js";
import "../styles/NotificationDropdown.css";

function formatType(n) {
  const who = n.triggererName ?? "Someone";
  switch (n.type) {
    case "FRIEND_REQUEST": return `${who} sent you a friend request`;
    case "COMMENT_REPLY": return `${who} replied to your comment`;
    case "MILESTONE": return `${who} you reached a milestone!`;
    default: return n.type;
  }
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString("en-GB", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

export default function NotificationDropdown() {
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);

  async function handleBellClick() {
    if (open) {
        setOpen(false);
        return;
    }

    setOpen(true);
    setLoading(true);

    try {
        const data = await getNotifications();
        console.log(data);
        setNotifications(data);
        const unread = data.filter((n) => !n.read).length;
        setUnreadCount(unread);

        if (unread > 0) {
            await markAllNotificationsRead();
            setUnreadCount(0);
            setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
        }
    } catch (err) {
        console.error("Failed to fetch notifications:", err);
    } finally {
        setLoading(false);
    }
}

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="notif-wrapper" ref={dropdownRef}>
      {/* Bell button */}
      <button className="notif-bell-btn" onClick={handleBellClick}>
        <BellIcon className="notif-bell-icon" />
        {unreadCount > 0 && (
          <span className="notif-badge">{unreadCount > 9 ? "9+" : unreadCount}</span>
        )}
      </button>

      {/* Dropdown */}
      {open && (
        <div className="notif-dropdown">
          <div className="notif-dropdown-header">
            <span className="notif-dropdown-title">Notifications</span>
          </div>

          {loading ? (
            <p className="notif-empty">Loading...</p>
          ) : notifications.length === 0 ? (
            <p className="notif-empty">No notifications yet.</p>
          ) : (
            <ul className="notif-list">
              {notifications.map((n) => (
                <li
                  key={n.id}
                  className={`notif-item ${!n.read ? "notif-item--unread" : ""}`}
                >
                  <div className="notif-dot-row">
                    {!n.read && <span className="notif-dot" />}
                    <p className="notif-text">{formatType(n)}</p>
                  </div>
                  <span className="notif-date">{formatDate(n.createdAt)}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}