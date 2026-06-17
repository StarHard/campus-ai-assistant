/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        notion: {
          'canvas': '#ffffff',
          'canvas-soft': '#f6f5f4',
          'surface': '#ffffff',
          'ink': '#000000',
          'ink-secondary': '#31302e',
          'ink-muted': '#615d59',
          'ink-faint': '#a39e98',
          'hairline': '#e6e6e6',
          'primary': '#0075de',
          'primary-active': '#005bab',
          'secondary': '#213183',
          'on-primary': '#ffffff',
          'accent-sky': '#62aef0',
          'accent-purple': '#d6b6f6',
          'accent-pink': '#ff64c8',
          'accent-orange': '#dd5b00',
          'accent-teal': '#2a9d99',
          'accent-green': '#1aae39',
          'accent-brown': '#523410',
        },
      },
      fontFamily: {
        'notion': ['Inter', '-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Helvetica', 'Arial', 'sans-serif'],
      },
      borderRadius: {
        'notion-xs': '4px',
        'notion-sm': '5px',
        'notion-md': '8px',
        'notion-lg': '12px',
        'notion-xl': '16px',
        'notion-full': '9999px',
      },
      boxShadow: {
        'notion-1': 'rgba(0,0,0,0.01) 0 0.175px 1.041px, rgba(0,0,0,0.02) 0 0.8px 2.925px, rgba(0,0,0,0.027) 0 2.025px 7.847px, rgba(0,0,0,0.04) 0 4px 18px',
        'notion-2': 'rgba(0,0,0,0.01) 0 0.175px 1.041px, rgba(0,0,0,0.02) 0 0.8px 2.925px, rgba(0,0,0,0.027) 0 2.025px 7.847px, rgba(0,0,0,0.04) 0 4px 18px, rgba(0,0,0,0.05) 0 23px 52px',
      },
      animation: {
        'typing': 'typing 0.5s ease-in-out',
        'fade-in': 'fadeIn 0.3s ease-in-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'pulse-slow': 'pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      },
      keyframes: {
        typing: {
          '0%': { opacity: '0', transform: 'translateY(5px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
    },
  },
  plugins: [],
}
